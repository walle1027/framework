package org.loed.framework.r2dbc;

import org.loed.framework.r2dbc.autoconfigure.R2dbcDaoScanner;
import org.loed.framework.r2dbc.autoconfigure.R2dbcDbInspector;
import org.loed.framework.r2dbc.listener.TestPostInsertListener;
import org.loed.framework.r2dbc.listener.TestPreDeleteListener;
import org.loed.framework.r2dbc.listener.spi.PostInsertListener;
import org.loed.framework.r2dbc.listener.spi.PreDeleteListener;
import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/4/30 4:03 PM
 */
@SpringBootApplication
@R2dbcDaoScanner(basePackages = "org.loed.framework.r2dbc.dao")
@R2dbcDbInspector(basePackages = "org.loed.framework.r2dbc.po")
public class R2dbcApplication {
	public static void main(String[] args) {
		SpringApplication.run(R2dbcApplication.class, args);
	}

//	@Override
//	@Bean
//	public ConnectionFactory connectionFactory() {
//		ConnectionFactoryOptions.Builder builder = ConnectionFactoryOptions.builder();
//		builder.option(ConnectionFactoryOptions.HOST, "127.0.0.1")
//				.option(ConnectionFactoryOptions.PORT, 3306)
//				.option(ConnectionFactoryOptions.USER, "root")
//				.option(ConnectionFactoryOptions.PASSWORD, "123456")
//				.option(ConnectionFactoryOptions.DATABASE, "test")
//
//
//		;
//		return ConnectionFactories.get(builder.build());
//	}

	@Bean
	PreInsertListener defaultPreInsertListener2() {
		return new DefaultPreInsertListener2();
	}

	@Bean
	PostInsertListener testPostInsertListener() {
		return new TestPostInsertListener();
	}

	@Bean
	PostInsertListener testPostInsertListener2() {
		return new TestPostInsertListener();
	}

	@Bean
	PreDeleteListener testPreDeleteListener() {
		return new TestPreDeleteListener();
	}
}
