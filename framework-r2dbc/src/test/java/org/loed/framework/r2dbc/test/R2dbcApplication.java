package org.loed.framework.r2dbc.test;

import org.loed.framework.r2dbc.autoconfigure.R2dbcDaoScanner;
import org.loed.framework.r2dbc.autoconfigure.R2dbcDbInspector;
import org.loed.framework.r2dbc.listener.TestPostInsertListener;
import org.loed.framework.r2dbc.listener.TestPreDeleteListener;
import org.loed.framework.r2dbc.listener.spi.PostInsertListener;
import org.loed.framework.r2dbc.listener.spi.PreDeleteListener;
import org.loed.framework.r2dbc.test.dao.LongIdDao;
import org.loed.framework.r2dbc.test.po.LongId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/4/30 4:03 PM
 */
@SpringBootApplication
@R2dbcDaoScanner(basePackageClasses = LongIdDao.class)
@R2dbcDbInspector(basePackageClasses = LongId.class)
public class R2dbcApplication {
	public static void main(String[] args) {
		SpringApplication.run(R2dbcApplication.class, args);
	}

	@Bean
	PostInsertListener testPostInsertListener() {
		return new TestPostInsertListener(1);
	}

	@Bean
	PostInsertListener testPostInsertListener2() {
		return new TestPostInsertListener(2);
	}

	@Bean
	PreDeleteListener testPreDeleteListener() {
		return new TestPreDeleteListener();
	}
}
