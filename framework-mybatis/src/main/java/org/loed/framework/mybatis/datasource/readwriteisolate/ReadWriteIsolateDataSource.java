package org.loed.framework.mybatis.datasource.readwriteisolate;

import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.mybatis.datasource.creator.DataSourceCreator;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.readwriteisolate.provider.ReadWriteDataSourceProvider;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/6/15 3:38 PM
 */
@Slf4j
public class ReadWriteIsolateDataSource implements DataSource {

	private final ReadWriteDataSourceProvider readWriteDataSourceProvider;

	private static final ConcurrentHashMap<String, DataSource> DATA_SOURCE_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

	private final DataSourceCreator dataSourceCreator;

	public ReadWriteIsolateDataSource(ReadWriteDataSourceProvider readWriteDataSourceProvider, DataSourceCreator dataSourceCreator) {
		this.readWriteDataSourceProvider = readWriteDataSourceProvider;
		this.dataSourceCreator = dataSourceCreator;
	}

	private ReadWriteStrategy getReadWriteStrategy() {
		ReadWriteStrategy readWriteStrategy = ReadWriteContext.getRwType();
		if (readWriteStrategy == null) {
			log.debug("readWriteStrategy is null ,will use default " + ReadWriteStrategy.write.name() + " strategy");
			readWriteStrategy = ReadWriteStrategy.write;
		}
		return readWriteStrategy;
	}

	private DataSource getDelegatedDataSource() {
		ReadWriteStrategy readWriteStrategy = getReadWriteStrategy();
		switch (readWriteStrategy) {
			case read:
				DataSourceMetaInfo readDataSource = readWriteDataSourceProvider.getReadDataSource();
				String readKey = readWriteStrategy.name() + ":" + readDataSource.getJdbcUrl();
				log.debug("get " + ReadWriteStrategy.read.name() + " datasource from key:" + readKey + " datasource info:" + SerializeUtils.toJson(readDataSource));
				return DATA_SOURCE_CONCURRENT_HASH_MAP.computeIfAbsent(readKey, k1 -> dataSourceCreator.createDataSource(readDataSource));
			case write:
				DataSourceMetaInfo writeDataSource = readWriteDataSourceProvider.getWriteDataSource();
				String writeKey = readWriteStrategy.name() + ":" + writeDataSource.getJdbcUrl();
				log.debug("get " + ReadWriteStrategy.write.name() + " datasource from key:" + writeKey + " datasource info:" + SerializeUtils.toJson(writeDataSource));
				return DATA_SOURCE_CONCURRENT_HASH_MAP.computeIfAbsent(writeKey, k -> dataSourceCreator.createDataSource(writeDataSource));
			default:
				DataSourceMetaInfo defaultDataSource = readWriteDataSourceProvider.getWriteDataSource();
				String defaultKey = "default:" + defaultDataSource.getJdbcUrl();
				log.debug("get default datasource from key:" + defaultKey + " datasource info:" + SerializeUtils.toJson(defaultDataSource));
				return DATA_SOURCE_CONCURRENT_HASH_MAP.computeIfAbsent(defaultKey, k -> dataSourceCreator.createDataSource(defaultDataSource));
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getDelegatedDataSource().getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return getDelegatedDataSource().getConnection(username, password);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (T) this;
		}
		return getDelegatedDataSource().unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return (iface.isInstance(this) || getDelegatedDataSource().isWrapperFor(iface));
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return getDelegatedDataSource().getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		getDelegatedDataSource().setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		getDelegatedDataSource().setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return getDelegatedDataSource().getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}
}
