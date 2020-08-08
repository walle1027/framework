package org.loed.framework.mybatis.datasource.readwriteisolate.provider.support;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.loed.framework.common.balancer.Balancer;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;
import org.loed.framework.mybatis.datasource.readwriteisolate.provider.ReadWriteDataSourceProvider;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/6/18 9:37 AM
 */
@Slf4j
public class ZKReadWriteDataSourceProvider implements ReadWriteDataSourceProvider {

	private final CuratorFramework zkClient;

	private final String databaseName;

	private final Balancer<DataSourceMetaInfo> balancer;

	private static final String DATASOURCE_ROOT_PATH = "/RWDS";

	private DataSourceMetaInfo write;

	private List<DataSourceMetaInfo> reads;

	private PathChildrenCache pathChildrenCache;

	public ZKReadWriteDataSourceProvider(String databaseName, CuratorFramework zkClient, Balancer<DataSourceMetaInfo> balancer) {
		this.zkClient = zkClient;
		this.databaseName = databaseName;
		this.balancer = balancer;
		String path = DATASOURCE_ROOT_PATH + "/" + databaseName;
		pathChildrenCache = new PathChildrenCache(zkClient, path, true);
		this.init();
	}

	/**
	 * fetch read write data from zookeeper
	 */
	private void init() {
		pathChildrenCache.getListenable().addListener((client, event) -> {
			String path;
			switch (event.getType()) {
				case CONNECTION_SUSPENDED:
				case CONNECTION_RECONNECTED:
				case CONNECTION_LOST:
					break;
				case CHILD_ADDED:
					path = event.getData().getPath();
					loadData(path);
					break;
				case CHILD_UPDATED:
					path = event.getData().getPath();
					loadData(path);
					break;
				case CHILD_REMOVED:
					write = null;
					reads = null;
					break;
				case INITIALIZED:
					log.info("read write datasource for database:" + databaseName + " initialized");
					break;
				default:
					break;
			}
		});
		String path = DATASOURCE_ROOT_PATH + "/" + databaseName;
		String writePath = path + "/" + ReadWriteStrategy.write.name();
		String readPath = path + "/" + ReadWriteStrategy.read.name();
		List<ChildData> currentData = pathChildrenCache.getCurrentData();
		//first read data from zookeeper
		for (ChildData childData : currentData) {
			String childPath = childData.getPath();
			if (writePath.equals(childPath)) {
				try {
					String writeStr = new String(childData.getData(), "UTF-8");
					this.write = SerializeUtils.fromJson(writeStr, DataSourceMetaInfo.class);
				} catch (Exception e) {
					log.error("can't read write datasource from path " + childPath, e);
				}
			}
			if (readPath.equals(childPath)) {
				try {
					String readStr = new String(childData.getData(), "UTF-8");
					this.reads = SerializeUtils.fromJson(readStr, new TypeReference<List<DataSourceMetaInfo>>() {
					});
					this.balancer.updateProfiles(reads);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	}


	private void loadData(String path) throws Exception {
		String writePath = path + "/" + ReadWriteStrategy.write.name();
		if (zkClient.checkExists().forPath(writePath) != null) {
			byte[] writerBytes = zkClient.getData().forPath(writePath);
			if (writerBytes != null) {
				String dataSourceMetaInfo = new String(writerBytes, "UTF-8");
				DataSourceMetaInfo write = SerializeUtils.fromJson(dataSourceMetaInfo, DataSourceMetaInfo.class);
				Assert.isTrue(write != null, "dataSourceMetaInfo de-Serialize fail" + dataSourceMetaInfo);
				write.setStrategy(ReadWriteStrategy.write);
				this.write = write;
				log.info("get write datasource for :" + databaseName + " " + dataSourceMetaInfo);
			} else {
				log.error("no write datasource found for database ", databaseName);
			}
		} else {
			log.error("no write datasource found for database ", databaseName);
		}
		String readPath = path + "/" + ReadWriteStrategy.read.name();
		if (zkClient.checkExists().forPath(readPath) != null) {
			byte[] readersBytes = zkClient.getData().forPath(readPath);
			if (readersBytes != null) {
				String readersInfo = new String(readersBytes, "UTF-8");
				List<DataSourceMetaInfo> reads = SerializeUtils.fromJson(readersInfo, new TypeReference<List<DataSourceMetaInfo>>() {
				});
				Assert.isTrue(reads != null, "dataSourceMetaInfo de-Serialize fail" + readersInfo);
				for (DataSourceMetaInfo read : reads) {
					read.setStrategy(ReadWriteStrategy.read);
				}
				this.reads = reads;
				balancer.updateProfiles(reads);
				log.info("get read datasource ", readersInfo);
			} else {
				log.error("can't get read datasource for database", databaseName);
			}
		} else {
			log.error("can't get read datasource for database", databaseName);
		}
	}

	@Override
	public DataSourceMetaInfo getReadDataSource() {
		return balancer.select();
	}

	@Override
	public DataSourceMetaInfo getWriteDataSource() {
		return write;
	}
}
