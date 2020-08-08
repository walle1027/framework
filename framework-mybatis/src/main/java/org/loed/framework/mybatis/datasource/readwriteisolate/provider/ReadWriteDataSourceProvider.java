package org.loed.framework.mybatis.datasource.readwriteisolate.provider;


import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/6/15 5:16 PM
 */
public interface ReadWriteDataSourceProvider {
	DataSourceMetaInfo getReadDataSource();

	DataSourceMetaInfo getWriteDataSource();
}
