package org.loed.framework.r2dbc.dao;

import org.loed.framework.common.database.Table;
import reactor.util.function.Tuple2;

import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/24 9:58 上午
 */
public interface R2dbcSqlBuilder {
	/**
	 * 构建Insert 语句
	 *
	 * @param table 表结构元数据
	 * @return Insert 语句及参数列表及参数类型
	 */
	Tuple2<String, List<Tuple2<String, Class<?>>>> insert(Table table);

	Tuple2<String, List<Tuple2<String, Class<?>>>> update(Table table);

	Tuple2<String, List<Tuple2<String, Class<?>>>> batchInsert(Table table, int batchSize);
}
