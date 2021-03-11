package org.loed.framework.mybatis.datasource.meta.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.loed.framework.common.balancer.BalanceStrategy;
import org.loed.framework.common.balancer.Balancer;
import org.loed.framework.common.balancer.BalancerFactory;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.common.zookeeper.ZKHolder;
import org.loed.framework.mybatis.datasource.meta.BalancedDataSource;
import org.loed.framework.mybatis.datasource.meta.DataSourceGroup;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.meta.DatabaseMetaInfoProvider;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/8/21 16:53
 */

public class ZKDatabaseMetaInfoProvider implements DatabaseMetaInfoProvider, InitializingBean, DisposableBean {
	/**
	 * 路由数据源根路径
	 */
	private static final String ROOT_PATH = "/RDS";
	/**
	 * 路由规则路径
	 */
	private static final String ROUTING_RULE_PATH = "/ROUTING";
	/**
	 * 数据源路径
	 */
	private static final String DATASOURCE_PATH = "/DATASOURCE";

	private final Logger logger = LoggerFactory.getLogger(ZKDatabaseMetaInfoProvider.class);
	/**
	 * ZK 连接信息
	 */
	private String zkAddress;
	/**
	 * 数据库名称
	 */
	private String database;
	/**
	 * zkClient
	 */
	private CuratorFramework zkClient;
	/**
	 * 主从库的负载均衡策略
	 */
	private final BalanceStrategy strategy;
	/**
	 * 是否运行自动分配数据源
	 */
	private final boolean autoAllocate;
	/**
	 * 本地的路由Map
	 */
	private final ConcurrentHashMap<String, BalancedDataSource> routingMap = new ConcurrentHashMap<>(1);

	/**
	 * 本地的数据源Map
	 */
	private final ConcurrentHashMap<String, DataSourceGroup> dataSourceMap = new ConcurrentHashMap<>(1);

	public ZKDatabaseMetaInfoProvider(BalanceStrategy strategy, boolean autoAllocate) {
		this.strategy = strategy;
		this.autoAllocate = autoAllocate;
	}

	/**
	 * 获取数据库信息
	 * 根据当前上下文中读写数据库的标记获取数据源配置
	 *
	 * @param routingKey   水平切分键
	 * @param routingValue 水平切分值
	 * @return 数据源元信息
	 */
	@Override
	public DataSourceMetaInfo getDatabase(String routingKey, String routingValue) {
		BalancedDataSource balancedDataSource = routingMap.computeIfAbsent(uniqueKey(routingKey, routingValue), (k) -> {
			if (autoAllocate) {
				return createBalancedDataSource();
			}
			return null;
		});
		if (balancedDataSource == null) {
			throw new RuntimeException("can't find datasource for database:{" + database + "},with routingKey:{" + routingKey + "} and routingValue:{" + routingValue + "}");
		}
		return balancedDataSource.getMaster();
	}

	protected BalancedDataSource createBalancedDataSource() {
		Balancer<DataSourceGroup> balancer = BalancerFactory.getBalancer(BalanceStrategy.weightedRoundRobin);
		balancer.updateProfiles(this.dataSourceMap.values());
		DataSourceGroup dataSourceGroup = balancer.select();
		if (dataSourceGroup == null) {
			logger.error("database:{} has no datasource,can't allocate", database);
			throw new RuntimeException("database:{" + database + "} has no datasource,can't allocate");
		}
		BalancedDataSource dataSource = new BalancedDataSource(this.strategy);
		dataSource.update(dataSourceGroup);
		return dataSource;
	}

	/**
	 * 获取数据库信息
	 * 根据当前上下文中读写数据库的标记获取数据源配置
	 *
	 * @param routingKey        水平切分键
	 * @param routingValue      水平切分值
	 * @param readWriteStrategy 读写类型
	 * @return 数据源元信息
	 */
	@Override
	public DataSourceMetaInfo getDatabase(String routingKey, String routingValue, ReadWriteStrategy readWriteStrategy) {
		// autoSharding
		BalancedDataSource balancedDataSource = routingMap.computeIfAbsent(uniqueKey(routingKey, routingValue), (k) -> {
			if (autoAllocate) {
				return createBalancedDataSource();
			}
			return null;
		});
		if (balancedDataSource == null) {
			throw new RuntimeException("can't find datasource for database:{" + database + "},with routingKey:{" + routingKey + "} and routingValue:{" + routingValue + "}");
		}
		switch (readWriteStrategy) {
			case write:
				return balancedDataSource.getMaster();
			case read:
				return balancedDataSource.getSlave();
			default:
				return null;
		}
	}

	@Override
	public List<DataSourceMetaInfo> getAllDataSource() {
		return this.routingMap.values().stream().map(BalancedDataSource::getMaster).collect(Collectors.toList());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (zkClient == null) {
			zkClient = ZKHolder.get(zkAddress);
		}
		loadRule();
		monitorRule();

		loadDataSource();
		monitorDataSource();
	}

	/**
	 * 加载数据库路由信息配置
	 * 数据库路由信息存储如下
	 * <pre>
	 *  RDS
	 *   |
	 *   |
	 *   --ROUTING
	 *      |
	 *      |
	 *      --database
	 *        |
	 *        |
	 *        --路由key
	 *           |
	 *           |
	 *           -- 路由值 =>  {
	 *                             master:{
	 *                                  "name":"数据库名称,
	 *                                  "driverClass":"驱动类",
	 *                                  "jdbcUrl":"jdbc连接地址",
	 *                                  "username":"用户名",
	 *                                  "password":"密码",
	 *                                  "weight": "权重 int"
	 *                              }
	 *                             ,slaves:[
	 *                                  {
	 *                                      "name":"数据库名称,
	 * 	                                    "driverClass":"驱动类",
	 * 	                                    "jdbcUrl":"jdbc连接地址",
	 * 	                                    "username":"用户名",
	 * 	                                    "password":"密码",
	 * 	                                    "weight": "权重 int"
	 *                                  }
	 *                             ]
	 *                         }
	 * </pre>
	 */
	protected void loadRule() {
		String databaseRootPath = ROOT_PATH + ROUTING_RULE_PATH + "/" + database;
		try {
			ZKHolder.checkOrCreatePath(zkClient, databaseRootPath);
			List<String> routingKeys = zkClient.getChildren().forPath(databaseRootPath);
			if (CollectionUtils.isEmpty(routingKeys)) {
				logger.warn("database :{} ,has no configured routing rule", database);
				return;
			}
			for (String routingKey : routingKeys) {
				List<String> routingValues = zkClient.getChildren().forPath(routingKey);
				if (CollectionUtils.isEmpty(routingValues)) {
					logger.warn("routing key:{},for database :{} ,has no routing values", routingKey, database);
					continue;
				}
				for (String routingValue : routingValues) {
					String fullPath = databaseRootPath + "/" + routingKey + "/" + routingValue;
					byte[] bytes = zkClient.getData().forPath(fullPath);
					DataSourceGroup dataSourceGroup = SerializeUtils.fromJson(new String(bytes, StandardCharsets.UTF_8), DataSourceGroup.class);
					if (dataSourceGroup == null) {
						logger.warn("routing key:{},routing value:{},for database :{} ,has no routing rule", routingKey, routingValue, database);
						continue;
					}
					BalancedDataSource dataSource = routingMap.computeIfAbsent(uniqueKey(routingKey, routingValue), k -> {
						return new BalancedDataSource(this.strategy);
					});
					dataSource.update(dataSourceGroup);
				}

			}
		} catch (Exception e) {
			logger.error("error load routing rule for database:{},caused by {}", database, e.getMessage(), e);
		}
	}

	/**
	 * 监控路由规则变化 路由规则参考 {@link #loadRule} 中的定义
	 */
	protected void monitorRule() {
		String databaseRootPath = ROOT_PATH + ROUTING_RULE_PATH + "/" + database;
		try {
			ZKHolder.checkOrCreatePath(zkClient, databaseRootPath);

			CuratorCache watcher = CuratorCache.build(zkClient, databaseRootPath);
			watcher.listenable().addListener((t, o, n) -> {
				String path = n.getPath();
				String routingRulePath = path.substring(databaseRootPath.length() + 1);
				if (!isValidRoutingRulePath(routingRulePath)) {
					logger.warn("path :{} is not a valid routing rule path", routingRulePath);
					return;
				}
				byte[] data = n.getData();
				if (data == null || data.length == 0) {
					logger.warn("data in routing rule:{},is null", routingRulePath);
					return;
				}
				DataSourceGroup dataSourceGroup = SerializeUtils.fromJson(new String(data, StandardCharsets.UTF_8), DataSourceGroup.class);
				if (dataSourceGroup == null) {
					logger.warn("data in routing rule:{},is null", routingRulePath);
					return;
				}
				String[] splits = routingRulePath.split("/");
				String routingKey = splits[0];
				String routingValue = splits[1];
				BalancedDataSource balancedDataSource = routingMap.computeIfAbsent(uniqueKey(routingKey, routingValue), k -> {
					return new BalancedDataSource(this.strategy);
				});
				switch (t) {
					case NODE_CREATED:
					case NODE_CHANGED:
						balancedDataSource.update(dataSourceGroup);
						break;
					case NODE_DELETED:
						routingMap.remove(uniqueKey(routingKey, routingValue));
						break;
					default:
						break;
				}
			});
			watcher.start();
		} catch (Exception e) {
			logger.error("error monitor routing rule for database:{},caused by:{}", database, e.getMessage(), e);
		}
	}

	/**
	 * 加载应用数据源
	 * 应用数据源的结构为：
	 * <pre>
	 *    RDS
	 *     |
	 *     |
	 *     --DATASOURCE
	 *        |
	 *        |
	 *        -- database
	 *            |
	 *            |
	 *            -- group1
	 *            |
	 *            |
	 *            -- group2 => {
	 *                 master:{
	 * 	                  "name":"数据库名称,
	 * 	                  "driverClass":"驱动类",
	 * 	                  "jdbcUrl":"jdbc连接地址",
	 * 	                  "username":"用户名",
	 * 	                  "password":"密码",
	 * 	                  "weight": "权重 int"
	 *                  }
	 * 	             ,slaves:[
	 *                      {
	 * 	                      "name":"数据库名称,
	 * 	                        "driverClass":"驱动类",
	 * 	                        "jdbcUrl":"jdbc连接地址",
	 * 	                        "username":"用户名",
	 * 	                        "password":"密码",
	 * 	                        "weight": "权重 int"
	 *                      }
	 * 	              ]
	 *            }
	 * </pre>
	 */
	protected void loadDataSource() {
		String datasourceRootPath = ROOT_PATH + DATASOURCE_PATH + "/" + database;
		try {
			ZKHolder.checkOrCreatePath(zkClient, datasourceRootPath);
			List<String> groups = zkClient.getChildren().forPath(datasourceRootPath);
			if (CollectionUtils.isEmpty(groups)) {
				logger.warn("database :{} ,has no datasource", database);
				return;
			}
			for (String group : groups) {
				String fullPath = datasourceRootPath + "/" + group;
				byte[] bytes = zkClient.getData().forPath(fullPath);
				DataSourceGroup dataSourceGroup = SerializeUtils.fromJson(new String(bytes, StandardCharsets.UTF_8), DataSourceGroup.class);
				if (dataSourceGroup == null) {
					logger.warn("group :{},for database :{} ,has no routing rule", group, database);
					continue;
				}
				dataSourceMap.put(group, dataSourceGroup);
			}
		} catch (Exception e) {
			logger.error("error load datasource for database:{},caused by {}", database, e.getMessage(), e);
		}
	}

	/**
	 * 监控数据源变化 路由规则参考 {@link #loadDataSource} 中的定义
	 */
	protected void monitorDataSource() {
		String datasourceRootPath = ROOT_PATH + DATASOURCE_PATH + "/" + database;
		try {
			ZKHolder.checkOrCreatePath(zkClient, datasourceRootPath);

			CuratorCache watcher = CuratorCache.build(zkClient, datasourceRootPath);
			watcher.listenable().addListener((t, o, n) -> {
				String path = n.getPath();
				String group = path.substring(datasourceRootPath.length() + 1);
				byte[] data = n.getData();
				if (data == null || data.length == 0) {
					logger.warn("data in routing rule:{},is null", group);
					return;
				}
				DataSourceGroup dataSourceGroup = SerializeUtils.fromJson(new String(data, StandardCharsets.UTF_8), DataSourceGroup.class);
				if (dataSourceGroup == null) {
					logger.warn("data in routing rule:{},is null", group);
					return;
				}
				switch (t) {
					case NODE_CREATED:
					case NODE_CHANGED:
						dataSourceMap.put(group, dataSourceGroup);
						break;
					case NODE_DELETED:
						routingMap.remove(group);
						break;
					default:
						break;
				}
			});
			watcher.start();
		} catch (Exception e) {
			logger.error("error monitor datasource for database:{},caused by:{}", database, e.getMessage(), e);
		}
	}

	/**
	 * 判断路径是否是合法的 路由规则路径
	 *
	 * @param path 路径
	 * @return 是否合法
	 */
	protected boolean isValidRoutingRulePath(String path) {
		String[] split = path.split("/");
		return split.length == 2;
	}

	protected String uniqueKey(String routingKey, String routingValue) {
		return routingKey + ":" + routingValue;
	}


	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getZkAddress() {
		return zkAddress;
	}

	public void setZkAddress(String zkAddress) {
		this.zkAddress = zkAddress;
	}

	@Override
	public void destroy() throws Exception {
		zkClient.close();
	}
}
