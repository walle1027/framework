package org.loed.framework.jdbc.datasource.routing.impl;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import org.loed.framework.jdbc.datasource.meta.DatabaseMetaInfo;
import org.loed.framework.jdbc.datasource.routing.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/15 上午11:09
 */
public class DruidRoutingDataSource extends AbstractRoutingDataSource {
	private int maxActive = 200;
	private int minIdle = 10;
	private int initialSize = 10;
	private DruidDataSource dataSourceTemplate;

	@Override
	protected DataSource createTargetDatasource(DatabaseMetaInfo databaseMeta) throws Exception {
		DruidDataSource dataSource = new DruidDataSource();
		//设置主要属性
		dataSource.setDriverClassName(databaseMeta.getDriverClass());
		dataSource.setUrl(databaseMeta.getJdbcUrl());
		dataSource.setUsername(databaseMeta.getUsername());
		dataSource.setPassword(databaseMeta.getPassword());
		//设置次要属性
		if (dataSourceTemplate != null) {
			dataSource.setMaxActive(dataSourceTemplate.getMaxActive());
			dataSource.setMinIdle(dataSourceTemplate.getMinIdle());
			dataSource.setInitialSize(dataSourceTemplate.getInitialSize());
			dataSource.setTestWhileIdle(dataSourceTemplate.isTestWhileIdle());
			dataSource.setMaxWait(dataSourceTemplate.getMaxWait());
			dataSource.setLoginTimeout(dataSourceTemplate.getLoginTimeout());
			dataSource.setProxyFilters(dataSourceTemplate.getProxyFilters());
			dataSource.setKeepAlive(dataSourceTemplate.isKeepAlive());
			dataSource.setConnectProperties(dataSourceTemplate.getConnectProperties());
			dataSource.setEnable(dataSourceTemplate.isEnable());
			//TODO　copy a
		} else {
			dataSource.setMaxActive(maxActive);
			dataSource.setMinIdle(minIdle);
			dataSource.setInitialSize(initialSize);
			dataSource.setTestWhileIdle(true);
			List<Filter> filters = new ArrayList<>();
			Slf4jLogFilter slf4jLogFilter = new Slf4jLogFilter();
			slf4jLogFilter.setStatementExecutableSqlLogEnable(true);
			slf4jLogFilter.setResultSetLogEnabled(false);
			slf4jLogFilter.setConnectionLogEnabled(false);
			slf4jLogFilter.setDataSourceLogEnabled(false);
			filters.add(slf4jLogFilter);
			StatFilter statFilter = new StatFilter();
			statFilter.setSlowSqlMillis(3);
			statFilter.setMergeSql(true);
			filters.add(statFilter);
			dataSource.setProxyFilters(filters);
			//TODO 根据druid 数据源特性设置其他属性
		}
		return dataSource;
	}

	public DruidDataSource getDataSourceTemplate() {
		return dataSourceTemplate;
	}

	public void setDataSourceTemplate(DruidDataSource dataSourceTemplate) {
		this.dataSourceTemplate = dataSourceTemplate;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}
}
