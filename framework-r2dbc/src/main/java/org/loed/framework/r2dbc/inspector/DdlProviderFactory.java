package org.loed.framework.r2dbc.inspector;

import org.loed.framework.r2dbc.R2dbcDialect;
import org.loed.framework.r2dbc.autoconfigure.R2dbcProperties;
import org.loed.framework.r2dbc.inspector.dialect.MysqlProvider;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 11:57 上午
 */
public class DdlProviderFactory {
	private static DdlProviderFactory instance;
	private static ReentrantLock lock = new ReentrantLock();

	private DdlProviderFactory() {
	}


	public static DdlProviderFactory getInstance() {
		if (instance != null) {
			return instance;
		}
		lock.lock();
		try {
			instance = new DdlProviderFactory();
			return instance;
		} finally {
			lock.unlock();
		}
	}

	public DdlProvider getDdlProvider(R2dbcDialect dialect, R2dbcProperties properties) {
		switch (dialect) {
			case mysql:
				return new MysqlProvider(properties.isQuote());
			case postgres:
				return null;
			default:
				return null;
		}
	}
}
