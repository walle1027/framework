package org.loed.framework.mybatis.datasource.meta.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.mybatis.datasource.meta.BalancedDataSource;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.meta.DatabaseMetaInfoProvider;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于配置文件的数据源路由配置
 *
 * @author Thomason
 * @version 1.0
 * @since 2017/11/11 17:25
 */
@Slf4j
public class DefaultDatabaseMetaInfoProvider implements DatabaseMetaInfoProvider {
	private String name;
	/**
	 * 基于配置文件的数据源路由配置
	 * 在配置文件中按照如下格式配置数据源即可
	 * key:路由key#路由值
	 * value: 数据源
	 */
	private final Map<String, BalancedDataSource> configMap;

	public DefaultDatabaseMetaInfoProvider(Map<String, BalancedDataSource> configMap) {
		this.configMap = configMap;
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
		BalancedDataSource balancedDataSource = configMap.get(uniqueKey(routingKey, routingValue));
		if (balancedDataSource == null) {
			log.error("no datasource for routingKey:{} and routingValue:{}", routingKey, routingValue);
			return null;
		}
		return balancedDataSource.getMaster();
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
		BalancedDataSource balancedDataSource = configMap.get(uniqueKey(routingKey, routingValue));
		if (balancedDataSource == null) {
			log.error("no datasource for routingKey:{} and routingValue:{}", routingKey, routingValue);
			return null;
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
		return this.configMap.values().stream().map(BalancedDataSource::getMaster).collect(Collectors.toList());
	}

	public String buildKey(String tenantId, ReadWriteStrategy strategy) {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.isNotBlank(tenantId)) {
			builder.append(tenantId).append(".");
		}
		if (StringUtils.isNotBlank(name)) {
			builder.append(name).append(".");
		}
		if (strategy != null) {
			builder.append(strategy.name()).append(".");
		}
		return builder.deleteCharAt(builder.length() - 1).toString();
	}

	public static String uniqueKey(String routingKey, String routingValue) {
		return routingKey + "_#_" + routingValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, BalancedDataSource> getConfigMap() {
		return configMap;
	}

}
