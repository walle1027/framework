package org.loed.framework.mybatis.datasource.meta;

import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;

import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-2-25 下午3:27
 */

public interface DatabaseMetaInfoProvider {

	String DATASOURCE_PATH = "/RDS";

	/**
	 * 获取数据库信息
	 * 根据当前上下文中读写数据库的标记获取数据源配置
	 *
	 * @param horizontalKey   水平切分键
	 * @param horizontalValue 水平切分值
	 * @return 数据源元信息
	 */
	DatabaseMetaInfo getDatabaseMetaHorizontally(String horizontalKey, String horizontalValue);

	DatabaseMetaInfo getDatabaseMeta();

	DatabaseMetaInfo getDatabaseMeta(ReadWriteStrategy strategy);

	/**
	 * 获取数据库信息
	 * 根据当前上下文中读写数据库的标记获取数据源配置
	 *
	 * @param horizontalKey     水平切分键
	 * @param horizontalValue   水平切分值
	 * @param readWriteStrategy 读写类型
	 * @return 数据源元信息
	 */
	DatabaseMetaInfo getDatabaseMetaHorizontally(String horizontalKey, String horizontalValue, ReadWriteStrategy readWriteStrategy);


	/**
	 * 获取所有的数据源信息
	 *
	 * @return
	 */
	List<DatabaseMetaInfo> getAllMetaInfo();

	/**
	 * 根据jdbcurl连接字符串自动检测数据库驱动类型
	 *
	 * @param jdbcUrl jdbc连接地址
	 * @return jdbc驱动类
	 */
	default String autoDetectDriverClass(String jdbcUrl) {
		if (jdbcUrl.toLowerCase().startsWith("jdbc:postgresql")) {
			return "org.postgresql.Driver";
		} else if (jdbcUrl.toLowerCase().startsWith("jdbc:mysql")) {
			return "com.mysql.jdbc.Driver";
		} else if (jdbcUrl.toLowerCase().startsWith("jdbc:oracle")) {
			return "oracle.jdbc.driver.OracleDriver";
		} else if (jdbcUrl.toLowerCase().startsWith("jdbc:jtds:sqlserver")) {
			return "net.sourceforge.jtds.jdbc.Driver";
		}
		return null;
	}
}
