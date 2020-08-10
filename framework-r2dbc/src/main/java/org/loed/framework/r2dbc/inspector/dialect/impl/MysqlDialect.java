package org.loed.framework.r2dbc.inspector.dialect.impl;

import dev.miku.r2dbc.mysql.constant.DataTypes;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.database.schema.Table;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Index;
import org.loed.framework.common.util.DataType;
import org.loed.framework.r2dbc.inspector.dialect.DatabaseDialect;
import org.springframework.data.r2dbc.convert.ColumnMapRowMapper;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 12:42 下午
 */
@Slf4j
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

	private boolean quote;

	@Override
	public Mono<Table> getTable(Connection connection, @Nullable String catalog, @Nullable String schema, @NonNull String tableName) {
		String sql = "select * from information_schema.tables where TABLE_NAME ='" + tableName + "'";
		if (StringUtils.isNotBlank(catalog)) {
			sql += " and TABLE_CATALOG='" + catalog + "'";
		}
		if (StringUtils.isNotBlank(schema)) {
			sql += " and TABLE_SCHEMA = '" + schema + "'";
		}
		return Mono.from(connection.createStatement(sql).execute()).flatMap(result -> {
			return Mono.from(result.map(new ColumnMapRowMapper()));
		}).map(table -> {
			return (String) table.get("TABLE_NAME");
		}).flatMap(tableSqlName -> describeTable(connection, tableSqlName));
	}


	private Mono<Table> describeTable(Connection connection, String tableName) {
		return Mono.zip(Mono.from(connection.createStatement("describe " + tableName).execute()).onErrorResume(e -> {
			return Mono.empty();
		}).flatMapMany(result -> {
			return Flux.from(result.map(mysqlColumnMapper));
		}).collectList(), Mono.from(connection.createStatement("show index from " + tableName).execute()).onErrorResume(e -> {
			log.error(e.getMessage(), e);
			return Mono.empty();
		}).flatMapMany(result -> {
			return Flux.from(result.map(mysqlIndexMapper));
		}).collectList()).map(tup -> {
			Table table = new Table();
			List<org.loed.framework.common.database.schema.Column> columns = tup.getT1();
			List<MysqlIndex> mysqlIndices = tup.getT2();
			if (CollectionUtils.isNotEmpty(columns)) {
				for (org.loed.framework.common.database.schema.Column column : columns) {
					column.setTable(table);
				}
				table.setColumns(columns);
			}
			if (CollectionUtils.isNotEmpty(mysqlIndices)) {
				List<MysqlIndex> realIndices = mysqlIndices.stream().filter(row -> {
					String name = row.getKeyName();
					return !Objects.equals(name, "PRIMARY");
				}).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(realIndices)) {
					Map<String, List<MysqlIndex>> groupedIndex = realIndices.stream().collect(Collectors.groupingBy(MysqlIndex::getKeyName));
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
		builder.append("create table ").append(wrap(table.getSqlName()));
		builder.append("(");
		StringBuilder primaryKeys = new StringBuilder();
		table.getColumns().forEach(column -> {
			String columnName = column.getSqlName();
			if (isKeyword(columnName)) {
				throw new RuntimeException("columnName:" + column.getSqlName() + " is the keywords of mysql");
			}
			builder.append(wrap(columnName)).append(" ");
			builder.append(getColumnDefinition(column));
			builder.append(",");
			if (column.isPk()) {
				primaryKeys.append(wrap(columnName)).append(",");
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
		builder.append("alter table").append(" ").append(wrap(column.getTable().getSqlName()));
		builder.append(" ").append("add").append(" ");
		String columnName = column.getSqlName();
		builder.append(wrap(columnName));
		builder.append(" ");
		builder.append(getColumnDefinition(column));
		return builder.toString();
	}

	@Override
	public String addIndex(Index index) {
		if (index.getColumnList() == null) {
			return "";
		}
		String[] columnArr = index.getColumnList().split(",");
		//check column
		List<Column> columnList = index.getTable().getColumns();
		Set<String> columnSqlNameSet = columnList.stream().map(Column::getSqlName).collect(Collectors.toSet());
		Map<String, Column> columnJavaNameMap = columnList.stream().collect(Collectors.toMap(Column::getJavaName, v -> v, (a, b) -> a));
		List<String> indexColumnList = new ArrayList<>();
		for (String column : columnArr) {
			if (columnSqlNameSet.contains(column)) {
				indexColumnList.add(wrap(column));
				continue;
			}
			if (columnJavaNameMap.containsKey(column)) {
				indexColumnList.add(wrap(columnJavaNameMap.get(column).getSqlName()));
				continue;
			}
			throw new RuntimeException("column:" + column + " is not a valid column name");
		}
		return "create " +
				(index.isUnique() ? "unique" : "") + " " +
				"index" + " " +
				wrap(index.getName()) + " " +
				"on" + " " + wrap(index.getTable().getSqlName()) +
				"(" + String.join(",", indexColumnList) + ")";
	}

	@Override
	public boolean isQuote() {
		return quote;
	}

	@Override
	public String quote() {
		return "`";
	}

	public void setQuote(boolean quote) {
		this.quote = quote;
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
				case DataType.DT_Boolean:
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

	private final BiFunction<Row, RowMetadata, org.loed.framework.common.database.schema.Column> mysqlColumnMapper = ((row, rowMetadata) -> {
		org.loed.framework.common.database.schema.Column column = new org.loed.framework.common.database.schema.Column();

		String sqlName = (String) row.get("Field");
		String type = (String) row.get("Type");
		String aNull = (String) row.get("Null");
		String key = (String) row.get("Key");
		String defaultValue = (String) row.get("Default");
		// resolve sqlType
		int sqlType;
		String sqlTypeName;
		int size;
		int scare;
		boolean isPk = false;
		boolean nullable;
		boolean indexed = false;
		boolean unique = false;
		Assert.notNull(type, "type can't be null for column:" + sqlName);
		if (type.contains("(")) {
			String rawSqlType = type.substring(0, type.indexOf("("));
			//sdsfsdf
			Tuple2<String, Short> jdbcType = autoDetectType(rawSqlType);
			sqlType = jdbcType.getT2();
			sqlTypeName = jdbcType.getT1();
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
			Tuple2<String, Short> jdbcType = autoDetectType(type);
			sqlTypeName = jdbcType.getT1();
			sqlType = jdbcType.getT2();
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
		column.setSqlName(sqlName);
		column.setSqlType(sqlType);
		column.setSqlTypeName(sqlTypeName);
		column.setSize(size);
		column.setDecimalDigits(scare);
		column.setPrimaryKey(isPk);
		column.setIndexed(indexed);
		column.setNullable(nullable);
		column.setUnique(unique);
		column.setDefaultValue(defaultValue);
		return column;
	});

	@Data
	private static class MysqlIndex {
		private String table;
		private int nonUnique;
		private String keyName;
		private int sqlInIndex;
		private String columnName;
	}

	private final BiFunction<Row, RowMetadata, MysqlIndex> mysqlIndexMapper = ((row, rowMetadata) -> {
		MysqlIndex index = new MysqlIndex();
		index.setTable((String) row.get("Table"));
		index.setColumnName((String) row.get("Column_name"));
		index.setKeyName((String) row.get("Key_name"));
		index.setNonUnique(Integer.parseInt(String.valueOf(row.get("Non_unique"))));
		index.setSqlInIndex(Integer.parseInt(String.valueOf(row.get("Seq_in_index"))));
		return index;
	});

	/**
	 * @param mysqlRawType mysql 原始类型
	 * @return 标准化后的类型
	 * @see DataTypes
	 */
	private Tuple2<String, Short> autoDetectType(String mysqlRawType) {
		String mysqlRawTypeUpper = mysqlRawType.toUpperCase();
		switch (mysqlRawTypeUpper) {
			case "DECIMAL":
				return Tuples.of("DECIMAL", DataTypes.DECIMAL);
			case "TINYINT":
				return Tuples.of("TINYINT", DataTypes.TINYINT);
			case "SMALLINT":
				return Tuples.of("SMALLINT", DataTypes.SMALLINT);
			case "INT":
				return Tuples.of("INT", DataTypes.INT);
			case "FLOAT":
				return Tuples.of("FLOAT", DataTypes.FLOAT);
			case "DOUBLE":
				return Tuples.of("DOUBLE", DataTypes.DOUBLE);
			case "NULL":
				return Tuples.of("NULL", DataTypes.NULL);
			case "TIMESTAMP":
				return Tuples.of("TIMESTAMP", DataTypes.TIMESTAMP);
			case "BIGINT":
				return Tuples.of("BIGINT", DataTypes.BIGINT);
			case "MEDIUMINT":
				return Tuples.of("MEDIUMINT", DataTypes.MEDIUMINT);
			case "DATE":
				return Tuples.of("DATE", DataTypes.DATE);
			case "TIME":
				return Tuples.of("TIME", DataTypes.TIME);
			case "DATETIME":
				return Tuples.of("DATETIME", DataTypes.DATETIME);
			case "YEAR":
				return Tuples.of("YEAR", DataTypes.YEAR);
			case "VARCHAR":
				return Tuples.of("VARCHAR", DataTypes.VARCHAR);
			case "TINYTEXT":
				return Tuples.of("TINYTEXT", DataTypes.TINY_BLOB);
			case "TEXT":
				return Tuples.of("TEXT", DataTypes.BLOB);
			case "MEDIUMTEXT":
				return Tuples.of("MEDIUMTEXT", DataTypes.MEDIUM_BLOB);
			case "BIT":
				return Tuples.of("BIT", DataTypes.BIT);
			case "TIMESTAMP2":
				return Tuples.of("TIMESTAMP2", DataTypes.TIMESTAMP2);
			case "JSON":
				return Tuples.of("JSON", DataTypes.JSON);
			case "NEW_DECIMAL":
				return Tuples.of("NEW_DECIMAL", DataTypes.NEW_DECIMAL);
			case "ENUMERABLE":
				return Tuples.of("VARCHAR", DataTypes.ENUMERABLE);
			case "SET":
				return Tuples.of("VARCHAR", DataTypes.SET);
			case "TINY_BLOB":
				return Tuples.of("TINY_BLOB", DataTypes.TINY_BLOB);
			case "MEDIUM_BLOB":
				return Tuples.of("MEDIUM_BLOB", DataTypes.MEDIUM_BLOB);
			case "LONG_BLOB":
				return Tuples.of("LONG_BLOB", DataTypes.LONG_BLOB);
			case "BLOB":
				return Tuples.of("BLOB", DataTypes.BLOB);
			case "VARBINARY":
				return Tuples.of("VARBINARY", DataTypes.VARBINARY);
			case "STRING":
				return Tuples.of("STRING", DataTypes.STRING);
			case "GEOMETRY":
				return Tuples.of("GEOMETRY", DataTypes.GEOMETRY);
			default:
				log.error("unknown type of " + mysqlRawType);
				return null;
		}
	}
}
