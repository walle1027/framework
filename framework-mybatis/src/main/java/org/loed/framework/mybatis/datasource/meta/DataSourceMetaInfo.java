package org.loed.framework.mybatis.datasource.meta;


import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.balancer.Balanceable;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/8/21 18:38
 */
@Data
public class DataSourceMetaInfo implements Serializable, Balanceable {
	/**
	 * 数据库名称
	 */
	private String name;
	/**
	 * 驱动类
	 */
	private String driverClass;
	/**
	 * jdbcURL
	 */
	private String jdbcUrl;
	/**
	 * database username
	 */
	private String username;
	/**
	 * database password
	 */
	private String password;
	/**
	 * 负载
	 */
	private int weight = 1;
	/**
	 * 主库还是备库
	 */
	private ReadWriteStrategy strategy = ReadWriteStrategy.write;

	public DataSourceMetaInfo() {
	}

	/**
	 * 根据jdbcurl连接字符串自动检测数据库驱动类型
	 */
	public void autoDetectDriverClass() {
		if (StringUtils.isBlank(this.driverClass)) {
			if (jdbcUrl.toLowerCase().startsWith("jdbc:postgresql")) {
				this.driverClass = "org.postgresql.Driver";
			} else if (jdbcUrl.toLowerCase().startsWith("jdbc:mysql")) {
				this.driverClass = "com.mysql.jdbc.Driver";
			} else if (jdbcUrl.toLowerCase().startsWith("jdbc:oracle")) {
				this.driverClass = "oracle.jdbc.driver.OracleDriver";
			} else if (jdbcUrl.toLowerCase().startsWith("jdbc:jtds:sqlserver")) {
				this.driverClass = "net.sourceforge.jtds.jdbc.Driver";
			}
		}
	}

	public String getDatabaseKey() {
		return driverClass + ";" + jdbcUrl + ";" + username;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
		autoDetectDriverClass();
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


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DataSourceMetaInfo that = (DataSourceMetaInfo) o;
		return Objects.equals(name, that.name) &&
				Objects.equals(driverClass, that.driverClass) &&
				Objects.equals(jdbcUrl, that.jdbcUrl) &&
				Objects.equals(username, that.username);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, driverClass, jdbcUrl, username);
	}


	@Override
	public String toString() {
		return "DataSourceMetaInfo{" +
				", name='" + name + '\'' +
				", driverClass='" + driverClass + '\'' +
				", jdbcUrl='" + jdbcUrl + '\'' +
				", userName='" + username + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
