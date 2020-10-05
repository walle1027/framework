package org.loed.framework.r2dbc.test;

import org.loed.framework.r2dbc.autoconfigure.R2dbcDaoScanner;
import org.loed.framework.r2dbc.autoconfigure.R2dbcDbInspector;
import org.loed.framework.r2dbc.test.listener.TestPostInsertListener;
import org.loed.framework.r2dbc.test.listener.TestPreDeleteListener;
import org.loed.framework.r2dbc.test.listener.spi.PostInsertListener;
import org.loed.framework.r2dbc.test.listener.spi.PreDeleteListener;
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
