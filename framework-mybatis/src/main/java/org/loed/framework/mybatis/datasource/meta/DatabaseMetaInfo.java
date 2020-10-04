package org.loed.framework.mybatis.datasource.meta;

import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/8/21 18:38
 */
@Data
public class DatabaseMetaInfo implements Serializable {
	/**
	 * 数据库读写类型
	 */
	private ReadWriteStrategy strategy;
	/**
	 * 水平切分键
	 */
	private String horizontalShardingKey;
	/**
	 * 水平切分值
	 */
	private String horizontalShardingValue;
	/**
	 * 数据库名称
	 */
	private String database;
	/**
	 * jdbc驱动类
	 */
	private String driverClass;
	/**
	 * jdbc地址
	 */
	private String jdbcUrl;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 密码
	 */
	private String password;

	public DatabaseMetaInfo() {
	}

	public String getDatabaseKey() {
		return driverClass + ";" + jdbcUrl + ";" + username;
	}
}
