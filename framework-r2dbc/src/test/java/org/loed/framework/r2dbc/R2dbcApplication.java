package org.loed.framework.r2dbc;

import io.r2dbc.spi.ConnectionFactory;
import org.loed.framework.r2dbc.autoconfigure.R2dbcDaoScanner;
import org.loed.framework.r2dbc.autoconfigure.R2dbcDbInspector;
import org.loed.framework.r2dbc.listener.TestPreDeleteListener;
import org.loed.framework.r2dbc.listener.spi.PreDeleteListener;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilder;
import org.loed.framework.r2dbc.listener.TestPostInsertListener;
import org.loed.framework.r2dbc.listener.impl.DefaultPreInsertListener;
import org.loed.framework.r2dbc.listener.impl.DefaultPreUpdateListener;
import org.loed.framework.r2dbc.listener.spi.PostInsertListener;
import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
import org.loed.framework.r2dbc.listener.spi.PreUpdateListener;
import org.loed.framework.r2dbc.query.dialect.MysqlR2dbcSqlBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

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
	PreInsertListener defaultPreInsertListener() {
		DefaultPreInsertListener preInsertListener = new DefaultPreInsertListener();
		preInsertListener.setOrder(-1);
		return preInsertListener;
	}

	@Bean
	PreInsertListener defaultPreInsertListener2() {
		return new DefaultPreInsertListener2();
	}

	@Bean
	PreUpdateListener defaultPreUpdateListener() {
		return new DefaultPreUpdateListener();
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

	@Bean
	ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
		return new R2dbcTransactionManager(connectionFactory);
	}

	@Bean
	R2dbcSqlBuilder mysql() {
		return new MysqlR2dbcSqlBuilder(true);
	}
}
