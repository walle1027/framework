package org.loed.framework;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-29 下午12:35
 */

public class DbUnit {
	private static final Logger logger = LoggerFactory.getLogger(DbUnit.class);
	private String driverName = "org.postgresql.Driver";
	private String jdbcUrl = "jdbc:postgresql://192.168.0.213:5432/af";
	private String username = "postgres";
	private String password = "postgres";

	public DbUnit() {
		InputStream jdbc = null;
		Properties properties = new Properties();
		try {
			jdbc = getClass().getClassLoader().getResourceAsStream("jdbc.properties");
			properties.load(jdbc);
			setDriverName(properties.getProperty("jdbc.driverClassName"));
			setJdbcUrl(properties.getProperty("jdbc.url"));
			setUsername(properties.getProperty("jdbc.username"));
			setPassword(properties.getProperty("jdbc.password"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jdbc != null) {
				try {
					jdbc.close();
				} catch (IOException e) {
					jdbc = null;
					e.printStackTrace();
				}
			}
		}
	}

	private IDatabaseConnection createConnect() throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("create dbunit connection: " + jdbcUrl);
		}

		Class.forName(driverName);

		Connection jdbcConnection = DriverManager.getConnection(
				jdbcUrl, username, password);
		IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

		if (logger.isInfoEnabled()) {
			logger.info("connection create success");
		}
		return connection;
	}

	private ITableFilter createFilter() throws Exception {
		ITableFilter filter = new DatabaseSequenceFilter(createConnect());
		return filter;
	}

	public ITable queryTableData(String tableName, String querySQL) throws Exception {
		IDatabaseConnection connection = null;
		try {
			connection = createConnect();
			if (logger.isInfoEnabled()) {
				logger.info("execute query: " + querySQL);
			}
			ITable data = connection.createQueryTable(tableName, querySQL);
			return data;
		} finally {
			if (connection != null)
				connection.close();
		}
	}

	public void executeUpdate(String sql) throws Exception {
		IDatabaseConnection connection = null;
		try {
			connection = createConnect();
			if (logger.isInfoEnabled()) {
				logger.info("execute query: " + sql);
			}
			PreparedStatement pst = connection.getConnection().prepareStatement(sql);
			pst.executeUpdate();
			pst.close();
		} finally {
			if (connection != null)
				connection.close();
		}
	}

	public void exportTableDataToXML(Map<String, File> tableToXML) throws Exception {
		IDatabaseConnection connection = null;

		try {
			connection = createConnect();

			Set<String> tableSet = tableToXML.keySet();
			for (String tableName : tableSet) {
				QueryDataSet partialDataSet = new QueryDataSet(connection);
				partialDataSet.addTable(tableName);
				FlatXmlDataSet.write(partialDataSet, new FileOutputStream(tableToXML.get(tableName)));
			}
		} finally {
			if (connection != null)
				connection.close();
		}

	}

	public void insertTestData(File dataSetXML) throws Exception {
		execute(DatabaseOperation.INSERT, dataSetXML);
	}


	public void refreshTestData(File dataSetXML) throws Exception {
		execute(DatabaseOperation.REFRESH, dataSetXML);
	}

	public void removeTestData(File dataSetXML) throws Exception {
		FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
		IDataSet dataSet = builder.build(dataSetXML);
		dataSet = new FilteredDataSet(createFilter(), dataSet);
		DatabaseOperation.DELETE.execute(createConnect(), dataSet);
	}

	public void clearTable(File dataSetXML) throws Exception {
		execute(DatabaseOperation.DELETE_ALL, dataSetXML);
	}

	public void appendTestData(File dataSetXML) throws Exception {
		execute(DatabaseOperation.INSERT, dataSetXML);
	}

	public void execute(DatabaseOperation operation, File dataSetXML) throws Exception {
		IDatabaseConnection conn = null;
		try {
			conn = createConnect();

			FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
			IDataSet dataSet = builder.build(dataSetXML);
			operation.execute(conn, dataSet);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}


	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
