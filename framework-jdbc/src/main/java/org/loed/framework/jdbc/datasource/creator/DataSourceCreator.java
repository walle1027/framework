package org.loed.framework.jdbc.datasource.creator;


import org.loed.framework.jdbc.datasource.meta.DataSourceMetaInfo;

import javax.sql.DataSource;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/6/18 10:33 AM
 */
public interface DataSourceCreator {
	DataSource createDataSource(DataSourceMetaInfo metaInfo);
}
