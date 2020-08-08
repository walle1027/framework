package org.loed.framework.r2dbc.inspector.dialect;

import io.r2dbc.spi.Connection;
import org.loed.framework.common.database.schema.Table;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 11:57 上午
 */
public interface DatabaseDialect {
	Mono<Table> getTable(Connection connection, String tableName);

	String createTable(org.loed.framework.common.orm.Table table);

	String addColumn(org.loed.framework.common.orm.Column column);

	String addIndex(org.loed.framework.common.orm.Index index);
}
