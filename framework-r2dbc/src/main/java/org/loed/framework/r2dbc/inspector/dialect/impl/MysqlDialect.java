package org.loed.framework.r2dbc.inspector.dialect.impl;

import io.r2dbc.spi.Connection;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.database.schema.Table;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Index;
import org.loed.framework.common.util.DataType;
import org.loed.framework.r2dbc.inspector.dialect.DatabaseDialect;
import org.springframework.data.r2dbc.convert.ColumnMapRowMapper;
import reactor.core.publisher.Mono;

import java.sql.JDBCType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 12:42 下午
 */
public class MysqlDialect implements DatabaseDialect {
	private static final String[] KEYWORDS = new String[]{
			"ADD", "ALL", "ALTER",
			"ANALYZE", "AND", "AS",
			"ASC", "ASENSITIVE", "BEFORE",
			"BETWEEN", "BIGINT", "BINARY",
			"BLOB", "BOTH", "BY",
			"CALL", "CASCADE", "CASE",
			"CHANGE", "CHAR", "CHARACTER",
			"CHECK", "COLLATE", "COLUMN",
			"CONDITION", "CONNECTION", "CONSTRAINT",
			"CONTINUE", "CONVERT", "CREATE",
			"CROSS", "CURRENT_DATE", "CURRENT_TIME",
			"CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR",
			"DATABASE", "DATABASES", "DAY_HOUR",
			"DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND",
			"DEC", "DECIMAL", "DECLARE",
			"DEFAULT", "DELAYED", "DELETE",
			"DESC", "DESCRIBE", "DETERMINISTIC",
			"DISTINCT", "DISTINCTROW", "DIV",
			"DOUBLE", "DROP", "DUAL",
			"EACH", "ELSE", "ELSEIF",
			"ENCLOSED", "ESCAPED", "EXISTS",
			"EXIT", "EXPLAIN", "FALSE",
			"FETCH", "FLOAT", "FLOAT4",
			"FLOAT8", "FOR", "FORCE",
			"FOREIGN", "FROM", "FULLTEXT",
			"GOTO", "GRANT", "GROUP",
			"HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND",
			"HOUR_MINUTE", "HOUR_SECOND", "IF",
			"IGNORE", "IN", "INDEX",
			"INFILE", "INNER", "INOUT",
			"INSENSITIVE", "INSERT", "INT",
			"INT1", "INT2", "INT3",
			"INT4", "INT8", "INTEGER",
			"INTERVAL", "INTO", "IS",
			"ITERATE", "JOIN", "KEY",
			"KEYS", "KILL", "LABEL",
			"LEADING", "LEAVE", "LEFT",
			"LIKE", "LIMIT", "LINEAR",
			"LINES", "LOAD", "LOCALTIME",
			"LOCALTIMESTAMP", "LOCK", "LONG",
			"LONGBLOB", "LONGTEXT", "LOOP",
			"LOW_PRIORITY", "MATCH", "MEDIUMBLOB",
			"MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT",
			"MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD",
			"MODIFIES", "NATURAL", "NOT",
			"NO_WRITE_TO_BINLOG", "NULL", "NUMERIC",
			"ON", "OPTIMIZE", "OPTION",
			"OPTIONALLY", "OR", "ORDER",
			"OUT", "OUTER", "OUTFILE",
			"PRECISION", "PRIMARY", "PROCEDURE",
			"PURGE", "RAID0", "RANGE",
			"READ", "READS", "REAL",
			"REFERENCES", "REGEXP", "RELEASE",
			"RENAME", "REPEAT", "REPLACE",
			"REQUIRE", "RESTRICT", "RETURN",
			"REVOKE", "RIGHT", "RLIKE",
			"SCHEMA", "SCHEMAS", "SECOND_MICROSECOND",
			"SELECT", "SENSITIVE", "SEPARATOR",
			"SET", "SHOW", "SMALLINT",
			"SPATIAL", "SPECIFIC", "SQL",
			"SQLEXCEPTION", "SQLSTATE", "SQLWARNING",
			"SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT",
			"SSL", "STARTING", "STRAIGHT_JOIN",
			"TABLE", "TERMINATED", "THEN",
			"TINYBLOB", "TINYINT", "TINYTEXT",
			"TO", "TRAILING", "TRIGGER",
			"TRUE", "UNDO", "UNION",
			"UNIQUE", "UNLOCK", "UNSIGNED",
			"UPDATE", "USAGE", "USE",
			"USING", "UTC_DATE", "UTC_TIME",
			"UTC_TIMESTAMP", "VALUES", "VARBINARY",
			"VARCHAR", "VARCHARACTER", "VARYING",
			"WHEN", "WHERE", "WHILE",
			"WITH", "WRITE", "X509",
			"XOR", "YEAR_MONTH", "ZEROFILL",
	};

	@Override
	public Mono<Table> getTable(Connection connection, String tableName) {
		return Mono.zip(Mono.from(connection.createStatement("describe " + tableName).execute()).flatMapMany(result -> {
			return Mono.from(result.map(new ColumnMapRowMapper()));
		}).collectList(), Mono.from(connection.createStatement("show index from " + tableName).execute()).flatMapMany(result -> {
			return Mono.from(result.map((row, rowMetadata) -> {
				MysqlIndex index = new MysqlIndex();
				index.setTable((String) row.get("Table"));
				index.setColumnName((String) row.get("Column_name"));
				index.setKeyName((String) row.get("Key_name"));
				index.setNonUnique(Integer.parseInt(String.valueOf(row.get("Non_unique"))));
				index.setSqlInIndex(Integer.parseInt(String.valueOf(row.get("Sql_in_index"))));
				return index;
			}));
		}).collectList()).map(tup -> {
			Table table = new Table();
			//TODO
			List<Map<String, Object>> columns = tup.getT1();
			List<MysqlIndex> indexies = tup.getT2();
			if (CollectionUtils.isNotEmpty(columns)) {
				table.setColumns(columns.stream().map(row -> {
							String sqlName = (String) row.get("Filed");
							String type = (String) row.get("Type");
							String aNull = (String) row.get("Null");
							String key = (String) row.get("Key");
							Object defaultValue = row.get("Default");
							//TODO resolve sqlType
							int sqlType;
							String sqlTypeName;
							int size;
							int scare;
							boolean isPk = false;
							boolean nullable;
							boolean indexed = false;
							boolean unique = false;
							if (type.contains("(")) {
								String rawSqlType = type.substring(0, type.indexOf("("));
								JDBCType jdbcType = JDBCType.valueOf(rawSqlType.toUpperCase());
								sqlType = jdbcType.getVendorTypeNumber();
								sqlTypeName = jdbcType.getName();
								String suffix = type.substring(type.indexOf("(") + 1);
								if (suffix.contains(")")) {
									suffix = suffix.substring(0, suffix.indexOf(")"));
								}
								if (suffix.contains(",")) {
									size = Integer.parseInt(suffix.substring(0, suffix.indexOf(",")).trim());
									scare = Integer.parseInt(suffix.substring(suffix.indexOf(",") + 1).trim());
								} else {
									size = Integer.parseInt(suffix.trim());
									scare = 0;
								}
							} else {
								JDBCType jdbcType = JDBCType.valueOf(type.toUpperCase());
								sqlTypeName = jdbcType.getName();
								sqlType = jdbcType.getVendorTypeNumber();
								size = 0;
								scare = 0;
							}

							if (StringUtils.containsIgnoreCase(key, "PRI")) {
								isPk = true;
							}

							if (StringUtils.containsIgnoreCase(key, "MUL")) {
								indexed = true;
							} else if (StringUtils.containsIgnoreCase(key, "UNI")) {
								indexed = true;
								unique = true;
							}

							if (StringUtils.equalsIgnoreCase(aNull, "YES")) {
								nullable = true;
							} else if (StringUtils.equalsIgnoreCase(aNull, "NO")) {
								nullable = false;
							} else {
								nullable = false;
							}

							org.loed.framework.common.database.schema.Column column = new org.loed.framework.common.database.schema.Column(table);
							column.setSqlName(sqlName);
							column.setSqlType(sqlType);
							column.setSqlTypeName(sqlTypeName);
							column.setSize(size);
							column.setDecimalDigits(scare);
							column.setPrimaryKey(isPk);
							column.setIndexed(indexed);
							column.setNullable(nullable);
							column.setUnique(unique);
							column.setDefaultValue(defaultValue == null ? null : String.valueOf(defaultValue));
							return column;
						}).collect(Collectors.toList())
				);
			}
			if (CollectionUtils.isNotEmpty(indexies)) {
				List<MysqlIndex> realIndexies = indexies.stream().filter(row -> {
					String name = row.getKeyName();
					return !Objects.equals(name, "PRIMARY");
				}).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(realIndexies)) {
					Map<String, List<MysqlIndex>> groupedIndex = realIndexies.stream().collect(Collectors.groupingBy(MysqlIndex::getKeyName));
					table.setIndices(
							groupedIndex.entrySet().stream().map(entry -> {
								String key = entry.getKey();
								List<MysqlIndex> indexList = entry.getValue();
								org.loed.framework.common.database.schema.Index index = new org.loed.framework.common.database.schema.Index();
								index.setName(key);
								index.setUnique(indexList.get(0).getNonUnique() == 0);
								index.setColumnList(indexList.stream().sorted(Comparator.comparing(MysqlIndex::getSqlInIndex)).map(MysqlIndex::getColumnName).collect(Collectors.joining(",")));
								return index;
							}).collect(Collectors.toList())
					);
				}

			}
			return table;
		});
	}

	@Override
	public String createTable(org.loed.framework.common.orm.Table table) {
		if (isKeyword(table.getSqlName())) {
			throw new RuntimeException("tableName:" + table.getSqlName() + " is the keywords of mysql");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("create table ").append(table.getSqlName());
		builder.append("(");
		StringBuilder primaryKeys = new StringBuilder();
		table.getColumns().forEach(column -> {
			String columnName = column.getSqlName();
			if (isKeyword(columnName)) {
				throw new RuntimeException("columnName:" + column.getSqlName() + " is the keywords of mysql");
			}
			builder.append(columnName).append(" ");
			builder.append(getColumnDefinition(column));
			builder.append(",");
			if (column.isPk()) {
				primaryKeys.append(columnName).append(",");
			}
		});
		if (primaryKeys.length() > 0) {
			primaryKeys.deleteCharAt(primaryKeys.length() - 1);
			builder.append(" primary key (");
			builder.append(primaryKeys);
			builder.append(")");
		} else {
			builder.deleteCharAt(builder.length() - 1);
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public String addColumn(Column column) {
		if (column == null) {
			return null;
		}
		if (isKeyword(column.getSqlName())) {
			throw new RuntimeException("columnName:" + column.getSqlName() + " is the keywords of mysql");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("alter table").append(" ").append(column.getTable().getSqlName());
		builder.append(" ").append("add").append(" ");
		String columnName = column.getSqlName();
		builder.append(columnName);
		builder.append(" ");
		builder.append(getColumnDefinition(column));
		return builder.toString();
	}

	@Override
	public String addIndex(Index index) {
		return "create " +
				(index.isUnique() ? "unique" : " ") + " " +
				"index" + " " +
				index.getName() + " " +
				"on" + " " + index.getTable().getSqlName() +
				"(" + index.getColumnList() + ")";
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
					definition = "tinyint";
					break;
				case DataType.DT_int:
				case DataType.DT_Integer:
					definition = "int";
					break;
				case DataType.DT_Long:
				case DataType.DT_long:
				case DataType.DT_BigInteger:
					definition = "bigint";
					if (column.isPk()) {
						definition += " auto_increment ";
					}
					break;
				case DataType.DT_Double:
				case DataType.DT_double:
					definition = "double";
					break;
				case DataType.DT_Float:
				case DataType.DT_float:
					definition = "float";
					break;
				case DataType.DT_Character:
				case DataType.DT_char:
				case DataType.DT_String:
					definition = "varchar(" + column.getLength() + ")";
					break;
				case DataType.DT_Date:
				case DataType.DT_DateTime:
					definition = "datetime(6)";
					break;
				case DataType.DT_Boolean:
					definition = "tinyint";
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
		return definition;
	}

	private boolean isKeyword(String name) {
		for (String keyword : KEYWORDS) {
			if (keyword.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	@Data
	private static class MysqlColumn {
		private String table;
		private int nonUnique;
		private String keyName;
		private int sqlInIndex;
		private String columnName;
	}

	@Data
	private static class MysqlIndex {
		private String table;
		private int nonUnique;
		private String keyName;
		private int sqlInIndex;
		private String columnName;
	}
}
