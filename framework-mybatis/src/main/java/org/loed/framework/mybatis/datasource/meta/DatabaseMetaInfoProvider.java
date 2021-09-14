package org.loed.framework.mybatis.datasource.meta;

import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteStrategy;

import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-2-25 下午3:27
 */

public interface DatabaseMetaInfoProvider {



	/**
	 * 获取数据库信息
	 * 根据当前上下文中读写数据库的标记获取数据源配置
	 *
	 * @param routingKey   水平切分键
	 * @param routingValue 水平切分值
	 * @return 数据源元信息
	 */
	DataSourceMetaInfo getDatabase(String routingKey, String routingValue);

	/**
	 * 获取数据库信息
	 * 根据当前上下文中读写数据库的标记获取数据源配置
	 *
	 * @param routingKey        水平切分键
	 * @param routingValue      水平切分值
	 * @param readWriteStrategy 读写类型
	 * @return 数据源元信息
	 */
	DataSourceMetaInfo getDatabase(String routingKey, String routingValue, ReadWriteStrategy readWriteStrategy);

	/**
	 * 获取所有的数据源信息
	 *
	 * @return
	 */
	List<DataSourceMetaInfo> getAllDataSource();
}
