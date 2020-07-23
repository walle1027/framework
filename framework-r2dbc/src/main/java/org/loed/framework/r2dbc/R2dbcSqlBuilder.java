package org.loed.framework.r2dbc;

import org.loed.framework.common.ORMapping;
import org.loed.framework.common.database.Column;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.po.CommonPO;
import org.loed.framework.common.util.ReflectionUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/4/16 1:22 PM
 */
public class R2dbcSqlBuilder {
	private static final String BLANK = " ";

	public static <T extends CommonPO> Tuple2<String, Map<String, Object>> insert(T po) {
		Table table = ORMapping.get(po.getClass());
		if (table == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		Map<String, Object> params = new HashMap<>();
		builder.append("insert into ").append(table.getSqlName()).append(BLANK);
		builder.append("(");
		table.getColumns().stream().filter(Column::isInsertable).forEach(column -> {
			builder.append(BLANK).append(column.getSqlName()).append(",");
		});
		if (builder.charAt(builder.length() - 1) == ',') {
			builder.deleteCharAt(builder.length() - 1);
		}
		builder.append(")").append(BLANK).append("values").append(BLANK).append("(");
		table.getColumns().stream().filter(Column::isInsertable).forEach(column -> {
			builder.append(BLANK).append("?").append(column.getJavaName()).append(",");
			Object value = ReflectionUtils.getFieldValue(po, column.getJavaName());
			if (value != null && (ReflectionUtils.isSubClass(value.getClass(), Date.class)
					|| value.getClass().getName().equals(Date.class.getName()))) {
				Date date = (Date) value;
				LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault());
				params.put(column.getJavaName(), ldt);
			} else {
				params.put(column.getJavaName(), value);
			}
		});
		if (builder.charAt(builder.length() - 1) == ',') {
			builder.deleteCharAt(builder.length() - 1);
		}
		builder.append(")");
		return Tuples.of(builder.toString(), params);
	}
}
