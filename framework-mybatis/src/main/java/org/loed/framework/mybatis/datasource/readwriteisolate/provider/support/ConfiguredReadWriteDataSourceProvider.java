package org.loed.framework.mybatis.datasource.readwriteisolate.provider.support;


import org.loed.framework.common.balancer.Balancer;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;
import org.loed.framework.mybatis.datasource.readwriteisolate.provider.ReadWriteDataSourceProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/6/18 9:38 AM
 */
public class ConfiguredReadWriteDataSourceProvider implements ReadWriteDataSourceProvider {
	private static final Map<String, List<DataSourceMetaInfo>> CONFIG_MAP = new ConcurrentHashMap<>();

	private final Balancer<DataSourceMetaInfo> balancer;

	public ConfiguredReadWriteDataSourceProvider(Balancer<DataSourceMetaInfo> balancer) {
		this.balancer = balancer;
	}

	public void setWriteDataSource(DataSourceMetaInfo dataSourceMetaInfo) {
		dataSourceMetaInfo.setStrategy(ReadWriteStrategy.write);
		CONFIG_MAP.put(ReadWriteStrategy.write.name(), Collections.singletonList(dataSourceMetaInfo));
	}

	public void setReadDataSource(List<DataSourceMetaInfo> dataSourceMetaInfoList) {
		for (DataSourceMetaInfo dataSourceMetaInfo : dataSourceMetaInfoList) {
			dataSourceMetaInfo.setStrategy(ReadWriteStrategy.read);
		}
		CONFIG_MAP.put(ReadWriteStrategy.read.name(), dataSourceMetaInfoList);
		balancer.updateProfiles(dataSourceMetaInfoList);
	}

	@Override
	public DataSourceMetaInfo getReadDataSource() {
		return balancer.select();
	}

	@Override
	public DataSourceMetaInfo getWriteDataSource() {
		return CONFIG_MAP.get(ReadWriteStrategy.write.name()).get(0);
	}
}
