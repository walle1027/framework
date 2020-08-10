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
	/**
	 * 根据 catalog schema tableName 查询表结构定义
	 *
	 * @param connection r2dbc 连接
	 * @param catalog    catalog
	 * @param schema     schema
	 * @param tableName  tableName
	 * @return 表定义，如果不存在，则返回空
	 */
	Mono<Table> getTable(Connection connection, @Nullable String catalog, @Nullable String schema, @NonNull String tableName);

	/**
	 * 根据表定义构造创建表的sql
	 *
	 * @param table 表定义
	 * @return 建表sql
	 */
	String createTable(org.loed.framework.common.orm.Table table);

	/**
	 * 根据列定义，构建add column 语句
	 *
	 * @param column 列定义
	 * @return add column 语句
	 */
	String addColumn(org.loed.framework.common.orm.Column column);

	/**
	 * 根据索引定义，构建添加索引的sql语句
	 *
	 * @param index 索引定义
	 * @return 增加索引的sql语句
	 */
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

	/**
	 * 包装表名称和列名称
	 *
	 * @param raw 原始的表名称或者列名称
	 * @return 包装后的表名或者列名
	 */
	default String wrap(@NonNull String raw) {
		if (isQuote()) {
			return quote() + raw + quote();
		}
		return raw;
	}
}
