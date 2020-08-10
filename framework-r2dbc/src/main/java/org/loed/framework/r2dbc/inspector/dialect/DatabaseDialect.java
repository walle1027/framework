package org.loed.framework.r2dbc.inspector.dialect;

import io.r2dbc.spi.Connection;
import org.loed.framework.common.database.schema.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 11:57 上午
 */
public interface DatabaseDialect {
	Mono<Table> getTable(Connection connection,@Nullable String catalog,@Nullable String schema, @NonNull String tableName);

	String createTable(org.loed.framework.common.orm.Table table);

	String addColumn(org.loed.framework.common.orm.Column column);

	String addIndex(org.loed.framework.common.orm.Index index);

	/**
	 * 是否需要引号
	 *
	 * @return true 需要 false 不需要
	 */
	boolean isQuote();

	/**
	 * 获取引号的值
	 *
	 * @return 引号的值
	 */
	String quote();

	default String wrap(String raw) {
		if (isQuote()) {
			return quote() + raw + quote();
		}
		return raw;
	}
}
