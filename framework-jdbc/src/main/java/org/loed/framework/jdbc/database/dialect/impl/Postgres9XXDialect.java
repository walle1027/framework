package org.loed.framework.jdbc.database.dialect.impl;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Index;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.util.DataType;
import org.loed.framework.jdbc.database.dialect.Dialect;

import javax.persistence.GenerationType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/8/27 下午2:35
 */
public class Postgres9XXDialect implements Dialect {
	@Override
	public List<String> buildCreateTableClause(Table table) {
		List<String> result = new ArrayList<>();
		Column pkColumn = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		if (pkColumn != null && GenerationType.AUTO.equals(pkColumn.getIdGenerationType())) {
			result.add("DROP SEQUENCE IF EXISTS " + table.getSqlName() + "_seq");
			result.add("CREATE SEQUENCE " + table.getSqlName() + "_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE").append(BLANK).append(table.getSqlName())
				.append(BLANK).append("(");
		builder.append(
				table.getColumns().stream().map(column -> {
					return BLANK + column.getSqlName() + BLANK + getColumnDefinition(column) + BLANK;
				}).collect(Collectors.joining(","))
		);
		String primaryKeys = table.getColumns().stream().filter(Column::isPk).map(Column::getSqlName).collect(Collectors.joining(","));
		if (StringUtils.isNotBlank(primaryKeys)) {
			builder.append(BLANK).append(",PRIMARY KEY (").append(primaryKeys).append(")").append(BLANK);
		}
		builder.append(")");
		result.add(builder.toString());
		return result;
	}

	@Override
	public List<String> buildAddColumnClause(Column column) {
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE").append(BLANK).append(column.getTable().getSqlName());
		builder.append(BLANK).append("ADD COLUMN").append(BLANK);
		String columnName = column.getSqlName();
		builder.append(columnName);
		builder.append(BLANK);
		builder.append(getColumnDefinition(column));
		return Collections.singletonList(builder.toString());
	}

	@Override
	public List<String> buildUpdateColumnClause(Column column) {
		return null;
	}

	@Override
	public List<String> buildIndexClause(Index index) {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE INDEX").append(BLANK).append(index.getName()).append(BLANK)
				.append("ON").append(BLANK).append(index.getTable().getSqlName()).append(BLANK)
				.append("(").append(index.getColumnList()).append(")");
		return Collections.singletonList(builder.toString());
	}

	private String getColumnDefinition(Column column) {
		if (StringUtils.isNotBlank(column.getColumnDefinition())) {
			return column.getColumnDefinition();
		}
		String definition = "";
		int dataType = DataType.getDataType(column.getJavaType());
		if (DataType.isSimpleType(dataType)) {
			switch (dataType) {
				case DataType.DT_Byte:
				case DataType.DT_short:
					definition = "smallint";
					break;
				case DataType.DT_int:
				case DataType.DT_Integer:
					definition = "integer";
					break;
				case DataType.DT_Long:
				case DataType.DT_long:
				case DataType.DT_BigInteger:
					definition = "bigint";
					break;
				case DataType.DT_Double:
				case DataType.DT_double:
					definition = "numeric(18,8)";
					break;
				case DataType.DT_Float:
				case DataType.DT_float:
					definition = "decimal(18,8)";
					break;
				case DataType.DT_Character:
				case DataType.DT_char:
				case DataType.DT_String:
					definition = "varchar(" + column.getLength() + ")";
					break;
				case DataType.DT_Date:
				case DataType.DT_DateTime:
					definition = "timestamp";
					break;
				case DataType.DT_Boolean:
					definition = "smallint";
					break;
				case DataType.DT_BigDecimal:
					definition = "decimal(" + column.getLength() + "," + column.getScale() + ")";
					break;
				default:
					definition = "varchar(255)";
					break;
			}
		} else {
			definition = "text";
		}
		if (!column.isNullable()) {
			definition += " not null";
		}
		if (column.getDefaultValue() != null) {
			definition += " default '" + column.getDefaultValue() + "'";
		}
		if (column.isPk() && GenerationType.AUTO.equals(column.getIdGenerationType())) {
			definition += " default nextval('" + column.getTable().getSqlName() + "_seq" + "')";
		}
		return definition;
	}
}
