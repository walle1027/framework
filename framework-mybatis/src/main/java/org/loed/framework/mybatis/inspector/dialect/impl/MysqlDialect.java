package org.loed.framework.mybatis.inspector.dialect.impl;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Index;
import org.loed.framework.common.orm.Table;
import org.loed.framework.mybatis.inspector.dialect.Dialect;

import java.sql.SQLType;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/8/13 上午10:24
 */
public class MysqlDialect implements Dialect {
	private static String[] keywords = new String[]{
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
	public List<String> buildCreateTableClause(Table table) {
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
			builder.append(columnName).append(BLANK);
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
		if (StringUtils.isNotBlank(table.getComment())) {
			List<String> sqls = new ArrayList<>();
			sqls.add(builder.toString());
			sqls.add("alter table " + table.getSqlName() + " comment '" + table.getComment() + "'");
			return sqls;
		}
		return Collections.singletonList(builder.toString());
	}

	@Override
	public List<String> buildAddColumnClause(Column column) {
		//ALTER TABLE table_name ADD field_name field_type
		if (column == null) {
			return null;
		}
		if (isKeyword(column.getSqlName())) {
			throw new RuntimeException("columnName:" + column.getSqlName() + " is the keywords of mysql");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("alter table").append(BLANK).append(column.getTable().getSqlName());
		builder.append(BLANK).append("add").append(BLANK);
		String columnName = column.getSqlName();
		builder.append(columnName);
		builder.append(BLANK);
		builder.append(getColumnDefinition(column));
		return Collections.singletonList(builder.toString());
	}

	@Override
	public List<String> buildUpdateColumnClause(Column column) {
		//alter table manual_record MODIFY `ORGANIZATION` VARCHAR(500) DEFAULT NULL
		if (column == null) {
			return null;
		}
		return Collections.singletonList("alter table" + BLANK + column.getTable().getSqlName() +
				BLANK + "modify" + BLANK +
				column.getSqlName() +
				BLANK +
				getColumnDefinition(column));
	}

	private String getColumnDefinition(Column column) {
		if (StringUtils.isNotBlank(column.getColumnDefinition())) {
			return column.getColumnDefinition();
		}
		String definition = "";
		//优先考虑列类型
		SQLType sqlType = column.getSqlType();
		if (sqlType == null){
			System.out.println("null sqlType for column:" + column);
		}
		switch (sqlType.getVendorTypeNumber()) {
			case Types.TINYINT:
			case Types.BIT:
				definition = "tinyint";
				break;
			case Types.SMALLINT:
				definition = "smallint";
				break;
			case Types.INTEGER:
				definition = "int";
				break;
			case Types.BIGINT:
				definition = "bigint";
				if (column.isPk()) {
					definition += " auto_increment ";
				}
				break;
			case Types.FLOAT:
				definition = "float";
				break;
			case Types.DOUBLE:
				definition = "double";
				break;
			case Types.NUMERIC:
			case Types.DECIMAL:
				definition = "decimal(" + column.getLength() + "," + column.getScale() + ")";
				break;
			case Types.CHAR:
				definition = "char(" + column.getLength() + ")";
				break;
			case Types.VARCHAR:
				definition = "varchar(" + column.getLength() + ")";
				break;
			case Types.LONGVARCHAR:
				definition = "text";
				break;
			case Types.DATE:
				definition = "date";
				break;
			case Types.TIME:
				definition = "time";
				break;
			case Types.TIMESTAMP:
				definition = "datetime(6)";
				break;
			default:
				definition = "varchar(255)";
				break;
		}

		if (!column.isNullable()) {
			definition += " not null";
		}
		if (column.getDefaultValue() != null) {
			definition += " default '" + column.getDefaultValue() + "'";
		}
		if (StringUtils.isNotBlank(column.getSqlComment())) {
			definition += " comment '" + column.getSqlComment() + "'";
		}
		return definition;
	}


	@Override
	public List<String> buildIndexClause(Index index) {
		return Collections.singletonList("create" + BLANK +
				(index.isUnique() ? "unique" : BLANK) + BLANK +
				"index" + BLANK +
				index.getName() + BLANK +
				"on" + BLANK + index.getTable().getSqlName() +
				"(" + index.getColumnList() + ")");
	}

	private boolean isKeyword(String name) {
		for (String keyword : keywords) {
			if (keyword.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
}
