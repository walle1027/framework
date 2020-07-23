package org.loed.framework.jdbc.datasource.meta.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.common.zookeeper.ZKHolder;
import org.loed.framework.jdbc.datasource.meta.DatabaseMetaInfo;
import org.loed.framework.jdbc.datasource.meta.DatabaseMetaInfoProvider;
import org.loed.framework.jdbc.datasource.readwriteisolate.ReadWriteStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/8/21 16:53
 */

public class ZKDatabaseMetaInfoProvider implements DatabaseMetaInfoProvider, InitializingBean, DisposableBean {
	private Logger logger = LoggerFactory.getLogger(ZKDatabaseMetaInfoProvider.class);
	//ZK 连接信息
	private String zkAddress;
	//数据库名称
	private String database;
	//zkClient
	private CuratorFramework zkClient;
	//本地的路由Map
	private ConcurrentHashMap<String, DatabaseMetaInfo> databaseMetaMap;

	@Override
	public DatabaseMetaInfo getDatabaseMeta() {
		return null;
	}

	@Override
	public DatabaseMetaInfo getDatabaseMeta(ReadWriteStrategy strategy) {
		return null;
	}

	/**
	 * 获取数据库信息
	 * 根据当前上下文中读写数据库的标记获取数据源配置
	 *
	 * @param horizontalKey   水平切分键
	 * @param horizontalValue 水平切分值
	 * @return 数据源元信息
	 */
	@Override
	public DatabaseMetaInfo getDatabaseMetaHorizontally(String horizontalKey, String horizontalValue) {
		String horizontalShardingKey = horizontalKey + ":" + horizontalValue;
		//TODO autoSharding
		return databaseMetaMap.computeIfAbsent(horizontalShardingKey, (k) -> {
			return null;
		});
	}

	/**
	 * 获取数据库信息
	 * 根据当前上下文中读写数据库的标记获取数据源配置
	 *
	 * @param horizontalKey     水平切分键
	 * @param horizontalValue   水平切分值
	 * @param readWriteStrategy 读写类型
	 * @return 数据源元信息
	 */
	@Override
	public DatabaseMetaInfo getDatabaseMetaHorizontally(String horizontalKey, String horizontalValue, ReadWriteStrategy readWriteStrategy) {
		String horizontalShardingKey = horizontalKey + ":" + horizontalValue + "#" + readWriteStrategy.name();
		//TODO autoSharding
		return databaseMetaMap.computeIfAbsent(horizontalShardingKey, (k) -> {
			return null;
		});
	}

	@Override
	public List<DatabaseMetaInfo> getAllMetaInfo() {
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (zkClient == null) {
			zkClient = ZKHolder.get(zkAddress);
		}
		this.databaseMetaMap = new ConcurrentHashMap<>();
		loadRule();
		monitorRule();
	}

	/**
	 * 加载数据库路由信息配置
	 * 数据库路由信息存储如下
	 * +/RDS
	 * --/database
	 * ----/水平切分键:水平切分值#write  路由详细信息
	 * ----/水平切分键:水平切分值#read   路由详细信息
	 */
	private void loadRule() {
		String appRootPath = DATASOURCE_PATH + "/" + database;
		try {
			ZKHolder.checkOrCreatePath(zkClient, appRootPath);

			List<String> children = zkClient.getChildren().forPath(appRootPath);
			if (CollectionUtils.isNotEmpty(children)) {
				for (String child : children) {
					String fullPath = appRootPath + "/" + child;
					byte[] bytes = zkClient.getData().forPath(fullPath);
					DatabaseMetaInfo databaseMetaInfo = SerializeUtils.fromJson(new String(bytes, "UTF-8"), DatabaseMetaInfo.class);
					if (databaseMetaInfo != null) {
						databaseMetaMap.put(child, databaseMetaInfo);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected void monitorRule() {
		final String appRootPath = DATASOURCE_PATH + "/" + database;
		try {
			ZKHolder.checkOrCreatePath(zkClient, appRootPath);

			TreeCache watcher = new TreeCache(
					zkClient,
					appRootPath
			);
			watcher.getListenable().addListener((c, event) -> {
				TreeCacheEvent.Type eventType = event.getType();
				switch (eventType) {
					case NODE_ADDED:
					case NODE_REMOVED:
					case NODE_UPDATED:
						List<String> children = c.getChildren().forPath(appRootPath);
						databaseMetaMap = resolveRoutingRule(appRootPath, children);
						break;
					case CONNECTION_LOST:
						break;
					case INITIALIZED:
						break;
					case CONNECTION_SUSPENDED:
						break;
					case CONNECTION_RECONNECTED:
						break;
					default:
						break;
				}
			});
			watcher.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ConcurrentHashMap<String, DatabaseMetaInfo> resolveRoutingRule(String appRootPath, List<String> children) throws Exception {
		ConcurrentHashMap<String, DatabaseMetaInfo> map = new ConcurrentHashMap<>();
		for (String child : children) {
			String fullPath = appRootPath + "/" + child;
			byte[] bytes = zkClient.getData().forPath(fullPath);
			DatabaseMetaInfo databaseMetaInfo = SerializeUtils.fromJson(new String(bytes, "utf-8"), DatabaseMetaInfo.class);
			if (databaseMetaInfo != null) {
				map.put(child, databaseMetaInfo);
			}
		}
		return map;
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
