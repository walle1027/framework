package org.loed.framework.mybatis.datasource.creator.druid;

import com.alibaba.druid.pool.DruidDataSource;
import org.loed.framework.mybatis.datasource.creator.DataSourceCreator;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/6/20 4:10 PM
 */
public class DruidDataSourceDecorator implements DataSourceCreator {
	private final DataSourceCreator creator;

	public DruidDataSourceDecorator(DataSourceCreator creator) {
		this.creator = creator;
	}

	@Override
	public DataSource createDataSource(DataSourceMetaInfo metaInfo) {
		DataSource dataSource = creator.createDataSource(metaInfo);
		try {
			//datasource need init before use
			((DruidDataSource) dataSource).init();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataSource;
	}
}
