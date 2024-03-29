package org.loed.framework.mybatis.datasource.routing;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.loed.framework.common.RoutingDataSource;
import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.meta.DatabaseMetaInfoProvider;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteContext;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-2-25 下午3:19
 */
public abstract class AbstractRoutingDataSource implements RoutingDataSource, DisposableBean {
	/**
	 * 内置的数据源Map
	 */
	private final Map<String, DataSource> dataSourceMap;
	private Logger logger = LoggerFactory.getLogger(AbstractRoutingDataSource.class);
	/**
	 * 数据源配置元数据提供者
	 */
	private DatabaseMetaInfoProvider metaInfoProvider;
	/**
	 * 水平切分key
	 */
	private String routingKey;
	/**
	 * 是否读写分离
	 */
	private boolean readWriteIsolate = true;
	private ReentrantLock lock = new ReentrantLock();

	public AbstractRoutingDataSource() {
		this.dataSourceMap = new ConcurrentHashMap<>();
	}

	/**
	 * <p>Attempts to establish a connection with the data source that
	 * this <code>DataSource</code> object represents.
	 *
	 * @return a connection to the data source
	 * @throws SQLException if a database access error occurs
	 */
	@Override
	public Connection getConnection() throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.getConnection();
	}

	/**
	 * <p>Attempts to establish a connection with the data source that
	 * this <code>DataSource</code> object represents.
	 *
	 * @param username the database user on whose behalf the connection is
	 *                 being made
	 * @param password the user's password
	 * @return a connection to the data source
	 * @throws SQLException if a database access error occurs
	 * @since 1.4
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.getConnection(username, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		DataSource dataSource = getDataSource();
		dataSource.setLogWriter(out);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.getLoginTimeout();
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		DataSource dataSource = getDataSource();
		dataSource.setLoginTimeout(seconds);
	}

	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.isWrapperFor(iface);
	}

	/**
	 * 取得数据源
	 *
	 * @return 当前线程变量中公司对应的数据源
	 * @throws SQLException 创建数据源异常或者无法找到对应公司的数据源时抛出的异常
	 */
	private DataSource getDataSource() throws SQLException {
		DataSourceMetaInfo dataSourceMetaInfo = null;
		//路由值
		String routingValue = getRoutingValue();
		if (readWriteIsolate) {
			ReadWriteStrategy readWriteStrategy = getReadWriteStrategy();
			dataSourceMetaInfo = metaInfoProvider.getDatabase(routingKey, routingValue, readWriteStrategy);
		} else {
			dataSourceMetaInfo = metaInfoProvider.getDatabase(routingKey, routingValue);
		}

		if (dataSourceMetaInfo == null) {
			throw new SQLException("Can't find data source");
		}
		return getDataSource(dataSourceMetaInfo);
	}

	private DataSource getDataSource(DataSourceMetaInfo dataSourceMetaInfo) throws SQLException {
		String databaseKey = dataSourceMetaInfo.getDatabaseKey();
		if (dataSourceMap.containsKey(databaseKey)) {
			return dataSourceMap.get(databaseKey);
		}
		lock.lock();
		try {
			if (dataSourceMap.containsKey(databaseKey)) {
				return dataSourceMap.get(databaseKey);
			}
			DataSource dataSource = createTargetDatasource(dataSourceMetaInfo);
			dataSourceMap.put(databaseKey, dataSource);
			return dataSource;
		} catch (Exception e) {
			throw new SQLException(e);
		} finally {
			lock.unlock();
		}
	}

	private String getRoutingValue() {
		return SystemContextHolder.get(routingKey);
	}

	@Override
	public List<DataSource> getAllDataSource() {
		List<DataSourceMetaInfo> metaInfoList = metaInfoProvider.getAllDataSource();
		if (metaInfoList != null) {
			List<DataSource> dataSourceList = new ArrayList<>();
			metaInfoList.stream().collect(Collectors.groupingBy(DataSourceMetaInfo::getDatabaseKey)).forEach(
					(k, v) -> {
						try {
							if (v != null && v.size() > 0) {
								dataSourceList.add(getDataSource(v.get(0)));
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
			);
			return dataSourceList;
		}
		return null;
	}

	private ReadWriteStrategy getReadWriteStrategy() {
		ReadWriteStrategy readWriteStrategy = ReadWriteContext.getRwType();
		if (readWriteStrategy == null) {
			readWriteStrategy = ReadWriteStrategy.write;
		}
		return readWriteStrategy;
	}

	public void setMetaInfoProvider(DatabaseMetaInfoProvider metaInfoProvider) {
		this.metaInfoProvider = metaInfoProvider;
	}

	protected abstract DataSource createTargetDatasource(DataSourceMetaInfo dataSourceMetaInfo) throws Exception;

	@Override
	public void destroy() throws Exception {
		Collection<DataSource> values = dataSourceMap.values();
		if (CollectionUtils.isNotEmpty(values)) {
			for (DataSource value : values) {
				closeDatasourceQuietly(value);
			}
		}
	}

	private void closeDatasourceQuietly(DataSource dataSource) {
		if (dataSource != null) {
			try {
				if (dataSource instanceof ComboPooledDataSource) {
					((ComboPooledDataSource) dataSource).close();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public boolean isReadWriteIsolate() {
		return readWriteIsolate;
	}

	public void setReadWriteIsolate(boolean readWriteIsolate) {
		this.readWriteIsolate = readWriteIsolate;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}
}
