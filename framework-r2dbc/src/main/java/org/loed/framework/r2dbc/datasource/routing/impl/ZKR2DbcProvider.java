package org.loed.framework.r2dbc.datasource.routing.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.zookeeper.CreateMode;
import org.loed.framework.common.balancer.BalanceStrategy;
import org.loed.framework.common.balancer.Balancer;
import org.loed.framework.common.balancer.BalancerFactory;
import org.loed.framework.common.balancer.FocusBalancer;
import org.loed.framework.common.lock.ZKDistributeLock;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.common.zookeeper.ZKHolder;
import org.loed.framework.r2dbc.datasource.MasterSlaveStrategy;
import org.loed.framework.r2dbc.datasource.R2dbcDataSource;
import org.loed.framework.r2dbc.datasource.R2dbcDataSourceGroup;
import org.loed.framework.r2dbc.datasource.routing.R2dbcProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/10 6:58 下午
 */
@Slf4j
public class ZKR2DbcProvider implements R2dbcProvider {
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

	/**
	 * 数据库名称
	 */
	private final String database;
	/**
	 * ZK 连接信息
	 */
	private final String zkAddress;
	/**
	 * zkClient
	 */
	private final CuratorFramework zkClient;
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
	private final ConcurrentHashMap<String, FocusBalancer<R2dbcDataSource>> routingMap = new ConcurrentHashMap<>(1);

	/**
	 * 本地的数据源Map
	 */
	private final ConcurrentHashMap<String, R2dbcDataSourceGroup> dataSourceMap = new ConcurrentHashMap<>(1);

	public ZKR2DbcProvider(String database, String zkAddress, boolean autoAllocate, BalanceStrategy strategy) {
		this.zkAddress = zkAddress;
		this.database = database;
		this.strategy = strategy;
		this.autoAllocate = autoAllocate;
		this.zkClient = ZKHolder.get(zkAddress);
	}


	@Override
	public Mono<R2dbcDataSource> getDataSource(String routingKey, String routingValue, MasterSlaveStrategy strategy) {
		FocusBalancer<R2dbcDataSource> r2dbcDataSourceBalancer = routingMap.computeIfAbsent(uniqueKey(routingKey, routingValue), k -> {
			if (this.autoAllocate) {
				// auto allocate
				return createRoutingRule(routingKey, routingValue);
			}
			return null;
		});
		if (r2dbcDataSourceBalancer == null) {
			return Mono.empty();
		}
		switch (strategy) {
			case master:
				return Mono.just(r2dbcDataSourceBalancer.getFocus());
			case slave:
				R2dbcDataSource slave = r2dbcDataSourceBalancer.select();
				return slave == null ? Mono.empty() : Mono.just(slave);
			default:
				return Mono.empty();
		}
	}

	private FocusBalancer<R2dbcDataSource> createRoutingRule(String routingKey, String routingValue) {
		String lockPath = database + ":" + uniqueKey(routingKey, routingValue);
		ZKDistributeLock lock = new ZKDistributeLock(zkClient);
		R2dbcDataSourceGroup rdsg = lock.get(lockPath, 5, TimeUnit.SECONDS, (k) -> {
			R2dbcDataSourceGroup dataSourceGroup = dataSourceMap.values().stream().max(Comparator.comparing(R2dbcDataSourceGroup::getWeight)).orElse(null);
			if (dataSourceGroup == null) {
				return null;
			}
			String databaseRootPath = ROOT_PATH + ROUTING_RULE_PATH + "/" + database;
			String routingRulePath = databaseRootPath + "/" + routingKey + "/" + routingValue;
			String jsonValue = SerializeUtils.toJson(dataSourceGroup);
			try {
				zkClient.create().withMode(CreateMode.PERSISTENT).forPath(routingRulePath, jsonValue.getBytes(StandardCharsets.UTF_8));
				return dataSourceGroup;
			} catch (Exception e) {
				log.error("error write value:{} for path:{}", jsonValue, routingRulePath, e);
			}
			return null;

		});
		if (rdsg == null) {
			log.error("can't allocate datasource for routingKey:{},routingValue:{}", routingKey, routingValue);
			return null;
		}
		Balancer<R2dbcDataSource> balancer = BalancerFactory.getBalancer(this.strategy);
		FocusBalancer<R2dbcDataSource> focusBalancer = new FocusBalancer<>(rdsg.getMaster(), balancer);
		if (rdsg.getSlaves() != null && rdsg.getSlaves().length > 0) {
			focusBalancer.updateProfiles(Arrays.asList(rdsg.getSlaves()));
		}
		return focusBalancer;
	}

	@Override
	public Flux<R2dbcDataSource> getAllDataSource() {
		return Flux.fromIterable(this.routingMap.values().stream().map(FocusBalancer::getFocus).collect(Collectors.toList()));
	}

	private String uniqueKey(String routingKey, String routingValue) {
		return routingKey + "#" + routingValue;
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
				log.warn("database :{} ,has no configured routing rule", database);
				return;
			}
			for (String routingKey : routingKeys) {
				List<String> routingValues = zkClient.getChildren().forPath(routingKey);
				if (CollectionUtils.isEmpty(routingValues)) {
					log.warn("routing key:{},for database :{} ,has no routing values", routingKey, database);
					continue;
				}
				for (String routingValue : routingValues) {
					String fullPath = databaseRootPath + "/" + routingKey + "/" + routingValue;
					byte[] bytes = zkClient.getData().forPath(fullPath);
					R2dbcDataSourceGroup dataSourceGroup = SerializeUtils.fromJson(new String(bytes, StandardCharsets.UTF_8), R2dbcDataSourceGroup.class);
					if (dataSourceGroup == null) {
						log.warn("routing key:{},routing value:{},for database :{} ,has no routing rule", routingKey, routingValue, database);
						continue;
					}
					FocusBalancer<R2dbcDataSource> dataSource = routingMap.computeIfAbsent(uniqueKey(routingKey, routingValue), k -> {
						Balancer<R2dbcDataSource> balancer = BalancerFactory.getBalancer(this.strategy);
						return new FocusBalancer<>(dataSourceGroup.getMaster(), balancer);
					});
					if (dataSourceGroup.getSlaves() != null && dataSourceGroup.getSlaves().length > 0) {
						dataSource.updateProfiles(Arrays.asList(dataSourceGroup.getSlaves()));
					}
				}

			}
		} catch (Exception e) {
			log.error("error load routing rule for database:{},caused by {}", database, e.getMessage(), e);
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
					log.warn("path :{} is not a valid routing rule path", routingRulePath);
					return;
				}
				byte[] data = n.getData();
				if (data == null || data.length == 0) {
					log.warn("data in routing rule:{},is null", routingRulePath);
					return;
				}
				R2dbcDataSourceGroup dataSourceGroup = SerializeUtils.fromJson(new String(data, StandardCharsets.UTF_8), R2dbcDataSourceGroup.class);
				if (dataSourceGroup == null) {
					log.warn("data in routing rule:{},is null", routingRulePath);
					return;
				}
				String[] splits = routingRulePath.split("/");
				String routingKey = splits[0];
				String routingValue = splits[1];
				FocusBalancer<R2dbcDataSource> balancedDataSource = routingMap.computeIfAbsent(uniqueKey(routingKey, routingValue), k -> {
					Balancer<R2dbcDataSource> balancer = BalancerFactory.getBalancer(this.strategy);
					return new FocusBalancer<>(dataSourceGroup.getMaster(), balancer);
				});
				switch (t) {
					case NODE_CREATED:
					case NODE_CHANGED:
						balancedDataSource.updateFocus(dataSourceGroup.getMaster());
						if (dataSourceGroup.getSlaves() != null && dataSourceGroup.getSlaves().length > 0) {
							balancedDataSource.updateProfiles(Arrays.asList(dataSourceGroup.getSlaves()));
						}
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
			log.error("error monitor routing rule for database:{},caused by:{}", database, e.getMessage(), e);
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
				log.warn("database :{} ,has no datasource", database);
				return;
			}
			for (String group : groups) {
				String fullPath = datasourceRootPath + "/" + group;
				byte[] bytes = zkClient.getData().forPath(fullPath);
				R2dbcDataSourceGroup dataSourceGroup = SerializeUtils.fromJson(new String(bytes, StandardCharsets.UTF_8), R2dbcDataSourceGroup.class);
				if (dataSourceGroup == null) {
					log.warn("group :{},for database :{} ,has no routing rule", group, database);
					continue;
				}
				dataSourceMap.put(group, dataSourceGroup);
			}
		} catch (Exception e) {
			log.error("error load datasource for database:{},caused by {}", database, e.getMessage(), e);
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
					log.warn("data in routing rule:{},is null", group);
					return;
				}
				R2dbcDataSourceGroup dataSourceGroup = SerializeUtils.fromJson(new String(data, StandardCharsets.UTF_8), R2dbcDataSourceGroup.class);
				if (dataSourceGroup == null) {
					log.warn("data in routing rule:{},is null", group);
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
			log.error("error monitor datasource for database:{},caused by:{}", database, e.getMessage(), e);
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
}
