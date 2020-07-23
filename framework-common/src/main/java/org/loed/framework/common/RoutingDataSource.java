package org.loed.framework.common;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/6/3 下午5:19
 */
public interface RoutingDataSource extends DataSource {
	List<DataSource> getAllDataSource();
}
