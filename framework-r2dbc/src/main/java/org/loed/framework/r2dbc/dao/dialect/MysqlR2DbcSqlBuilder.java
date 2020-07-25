package org.loed.framework.r2dbc.dao.dialect;

import org.loed.framework.common.database.Table;
import org.loed.framework.r2dbc.dao.R2dbcSqlBuilder;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/24 9:59 上午
 */
public class MysqlR2DbcSqlBuilder implements R2dbcSqlBuilder {

	@Override
	public Tuple2<String, List<Tuple2<String, Class<?>>>> insert(Table table) {
		return null;
	}

	@Override
	public Tuple2<String, List<Tuple2<String, Class<?>>>> update(Table table) {
		return null;
	}
}
