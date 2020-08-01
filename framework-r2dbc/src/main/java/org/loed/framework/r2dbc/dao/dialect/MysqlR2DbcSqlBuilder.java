package org.loed.framework.r2dbc.dao.dialect;

import org.loed.framework.common.database.Column;
import org.loed.framework.common.database.Table;
import org.loed.framework.r2dbc.dao.R2dbcSqlBuilder;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/24 9:59 上午
 */
public class MysqlR2DbcSqlBuilder implements R2dbcSqlBuilder {

	@Override
	public Tuple2<String, List<Tuple2<String, Class<?>>>> insert(Table table) {
		StringBuilder builder = new StringBuilder();
		List<Tuple2<String, Class<?>>> params = new ArrayList<>();
		builder.append("insert into ").append(table.getSqlName()).append("(");
		List<Column> columns = table.getColumns().stream().filter(Column::isInsertable).collect(Collectors.toList());
		columns.forEach(column -> {
			builder.append(column.getSqlName()).append(",");
			params.add(Tuples.of(column.getJavaName(), column.getJavaType()));
		});
		builder.deleteCharAt(builder.length() - 1);
		builder.append(") values ( ");
		columns.forEach(column -> {
			builder.append(":").append(column.getJavaName()).append(",");
		});
		builder.deleteCharAt(builder.length() - 1).append(")");
		return Tuples.of(builder.toString(), params);
	}

	@Override
	public Tuple2<String, List<Tuple2<String, Class<?>>>> update(Table table) {
		return null;
	}

	@Override
	public Tuple2<String, List<Tuple2<String, Class<?>>>> batchInsert(Table table, int batchSize) {
		StringBuilder builder = new StringBuilder();
		List<Tuple2<String, Class<?>>> params = new ArrayList<>();
		builder.append("insert into ").append(table.getSqlName()).append("(");
		List<Column> columns = table.getColumns().stream().filter(Column::isInsertable).collect(Collectors.toList());
		columns.forEach(column -> {
			builder.append(column.getSqlName()).append(",");
			params.add(Tuples.of(column.getJavaName(), column.getJavaType()));
		});
		builder.deleteCharAt(builder.length() - 1);
		builder.append(") values ");
		for (int i = 0; i < batchSize; i++) {
			builder.append("(");
			int finalI = i;
			columns.forEach(column -> {
				builder.append(":").append(column.getJavaName()).append(finalI).append(",");
			});
			builder.deleteCharAt(builder.length() - 1).append(")").append(",");
		}
		builder.deleteCharAt(builder.length() - 1);
		return Tuples.of(builder.toString(), params);
	}
}
