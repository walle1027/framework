package org.loed.framework.mybatis.datasource.meta.impl;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.mybatis.datasource.meta.DatabaseMetaInfo;
import org.loed.framework.mybatis.datasource.meta.DatabaseMetaInfoProvider;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于配置文件的数据源路由配置
 *
 * @author Thomason
 * @version 1.0
 * @since 2017/11/11 17:25
 */

public class DefaultDatabaseMetaInfoProvider implements DatabaseMetaInfoProvider {
	private String name;
	/**
	 * 基于配置文件的数据源路由配置
	 * 在配置文件中按照如下格式配置数据源即可
	 * 公司代码.应用代码.read/write(读写类型).jdbc.url=jdbc:postgresql://www.dev.com:5432/oms
	 * 公司代码.应用代码.read/write(读写类型).jdbc.username=postgres
	 * 公司代码.应用代码.read/write(读写类型).jdbc.password=postgres
	 */
	private Map<String, DatabaseMetaInfo> configMap;

	public DefaultDatabaseMetaInfoProvider() {
		this.configMap = new HashMap<>();
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
		return null;
	}

	@Override
	public DatabaseMetaInfo getDatabaseMeta() {
		return configMap.get(name);
	}

	@Override
	public DatabaseMetaInfo getDatabaseMeta(ReadWriteStrategy strategy) {
		String databaseKey = name + "#" + strategy;
		return configMap.get(databaseKey);
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
		return null;
	}

	@Override
	public List<DatabaseMetaInfo> getAllMetaInfo() {
		return null;
	}

	public String buildKey(String tenantCode, ReadWriteStrategy strategy) {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.isNotBlank(tenantCode)) {
			builder.append(tenantCode).append(".");
		}
		if (StringUtils.isNotBlank(name)) {
			builder.append(name).append(".");
		}
		if (strategy != null) {
			builder.append(strategy.name()).append(".");
		}
		return builder.deleteCharAt(builder.length() - 1).toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, DatabaseMetaInfo> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, DatabaseMetaInfo> configMap) {
		this.configMap = configMap;
	}
}
