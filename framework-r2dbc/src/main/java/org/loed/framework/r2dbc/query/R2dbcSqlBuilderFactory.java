package org.loed.framework.r2dbc.query;

import org.loed.framework.r2dbc.R2dbcDialect;
import org.loed.framework.r2dbc.autoconfigure.R2dbcProperties;
import org.loed.framework.r2dbc.query.dialect.MysqlR2dbcSqlBuilder;
import org.loed.framework.r2dbc.query.dialect.PostgresR2dbcSqlBuilder;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/11 10:28 上午
 */
public class R2dbcSqlBuilderFactory {
	private static R2dbcSqlBuilderFactory instance;
	private static ReentrantLock lock = new ReentrantLock();

	private R2dbcSqlBuilderFactory() {
	}

	public static R2dbcSqlBuilderFactory getInstance() {
		if (instance != null) {
			return instance;
		}
		lock.lock();
		try {
			instance = new R2dbcSqlBuilderFactory();
		} finally {
			lock.unlock();
		}
		return instance;
	}

	public R2dbcSqlBuilder getSqlBuilder(R2dbcDialect dialect, R2dbcProperties properties) {
		switch (dialect) {
			case mysql:
				return new MysqlR2dbcSqlBuilder(properties.isQuote());
			case postgres:
				return new PostgresR2dbcSqlBuilder(properties.isQuote());
			default:
				return null;
		}
	}
}
