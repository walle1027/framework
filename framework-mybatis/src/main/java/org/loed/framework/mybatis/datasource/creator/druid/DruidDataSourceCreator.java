package org.loed.framework.mybatis.datasource.creator.druid;

import com.alibaba.druid.pool.DruidDataSource;
import org.loed.framework.mybatis.datasource.creator.DataSourceCreator;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.meta.DruidProperties;

import javax.sql.DataSource;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/6/18 12:41 PM
 */
public class DruidDataSourceCreator implements DataSourceCreator {
	private DruidProperties druidProperties;

	public DruidDataSourceCreator(DruidProperties druidProperties) {
		this.druidProperties = druidProperties;
	}

	@Override
	public DataSource createDataSource(DataSourceMetaInfo databaseMeta) {
		DruidDataSource dataSource = new DruidDataSource();
		//设置主要属性
		dataSource.setDriverClassName(databaseMeta.getDriverClass());
		dataSource.setUrl(databaseMeta.getJdbcUrl());
		dataSource.setUsername(databaseMeta.getUserName());
		dataSource.setPassword(databaseMeta.getPassword());
		//设置次要属性
		dataSource.setMaxActive(druidProperties.getMaxActive());
		dataSource.setMinIdle(druidProperties.getMinIdle());
		dataSource.setInitialSize(druidProperties.getInitialSize());
		dataSource.setTestWhileIdle(druidProperties.isTestWhileIdle());
		dataSource.setTestOnBorrow(druidProperties.isTestOnBorrow());
		dataSource.setTestOnReturn(druidProperties.isTestOnReturn());
		dataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
		dataSource.setValidationQuery(druidProperties.getValidationQuery());
		//TODO add more
		return dataSource;
	}
}
