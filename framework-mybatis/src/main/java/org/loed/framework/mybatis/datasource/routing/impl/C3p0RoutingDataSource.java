package org.loed.framework.mybatis.datasource.routing.impl;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.routing.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/15 上午11:07
 */
public class C3p0RoutingDataSource extends AbstractRoutingDataSource {
	@Override
	protected DataSource createTargetDatasource(DataSourceMetaInfo databaseMeta) throws Exception {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setUser(databaseMeta.getUsername());
		dataSource.setJdbcUrl(databaseMeta.getJdbcUrl());
		dataSource.setDriverClass(databaseMeta.getDriverClass());
		dataSource.setPassword(databaseMeta.getPassword());
		return dataSource;
	}
}
