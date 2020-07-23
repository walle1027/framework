package org.loed.framework.jdbc.datasource.routing.impl;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.loed.framework.jdbc.datasource.meta.DatabaseMetaInfo;
import org.loed.framework.jdbc.datasource.routing.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/15 上午11:07
 */
public class C3p0RoutingDataSource extends AbstractRoutingDataSource {
	protected DataSource createTargetDatasource(DatabaseMetaInfo databaseMeta) throws Exception {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setUser(databaseMeta.getUsername());
		dataSource.setJdbcUrl(databaseMeta.getJdbcUrl());
		dataSource.setDriverClass(databaseMeta.getDriverClass());
		dataSource.setPassword(databaseMeta.getPassword());
		return dataSource;
	}
}
