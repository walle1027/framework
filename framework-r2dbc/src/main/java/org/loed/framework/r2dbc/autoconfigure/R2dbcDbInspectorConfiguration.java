package org.loed.framework.r2dbc.autoconfigure;

import org.loed.framework.r2dbc.inspector.dialect.DatabaseDialect;
import org.loed.framework.r2dbc.inspector.dialect.impl.MysqlDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/10 3:53 下午
 */
@Configuration
@EnableConfigurationProperties(R2dbcProperties.class)
public class R2dbcDbInspectorConfiguration {
	@Autowired
	private R2dbcProperties properties;

	@Bean
	@ConditionalOnMissingBean
	public DatabaseDialect mysqlDialect(){
		MysqlDialect dialect = new MysqlDialect();
		dialect.setQuote(properties.isQuote());
		return dialect;
	}
}
