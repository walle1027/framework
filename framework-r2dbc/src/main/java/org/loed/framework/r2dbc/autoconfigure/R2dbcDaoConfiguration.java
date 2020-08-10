package org.loed.framework.r2dbc.autoconfigure;

import org.loed.framework.r2dbc.query.R2dbcSqlBuilder;
import org.loed.framework.r2dbc.query.dialect.MysqlR2dbcSqlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/10 4:01 下午
 */
@Configuration
@EnableConfigurationProperties(R2dbcProperties.class)
public class R2dbcDaoConfiguration {
	@Autowired
	private R2dbcProperties properties;

	@Bean
	@ConditionalOnMissingBean
	R2dbcSqlBuilder mysqlR2dbcSqlBuilder() {
		return new MysqlR2dbcSqlBuilder(properties.isQuote());
	}
}
