package org.loed.framework.r2dbc.autoconfigure;

import io.r2dbc.spi.ConnectionFactory;
import org.loed.framework.r2dbc.inspector.R2dbcDbInspector;
import org.loed.framework.r2dbc.inspector.dialect.DatabaseDialect;
import org.loed.framework.r2dbc.inspector.dialect.impl.MysqlDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 11:43 上午
 */
@Configuration
@AutoConfigureAfter(ConnectionFactory.class)
public class R2dbcDbInspectorConfiguration {
	@Autowired
	private R2dbcProperties r2dbcProperties;

	@Bean
	@ConditionalOnMissingBean
	public DatabaseDialect dialect() {
		return new MysqlDialect();
	}

	@Bean
	public R2dbcDbInspector dbcDbInspector(ConnectionFactory connectionFactory, DatabaseDialect dialect) {
		R2dbcDbInspector r2dbcDbInspector = new R2dbcDbInspector(connectionFactory, dialect);
		r2dbcDbInspector.setEnabled(r2dbcProperties.getInspector().isEnabled());
		r2dbcDbInspector.setExecute(r2dbcProperties.getInspector().isExecute());
		return r2dbcDbInspector;
	}
}
