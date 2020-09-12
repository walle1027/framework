package org.loed.framework.common.orm.schema;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yangtao
 */
@Data
@Slf4j
public class Column {
	/**
	 * Reference to the containing table
	 */
	private Table table;
	/**
	 * The java.sql.Types type
	 */
	private int sqlType;
	/**
	 * The sql typename. provided by JDBC driver
	 */
	private String sqlTypeName;
	/**
	 * The name of the column
	 */
	private String sqlName;
	/**
	 * size of the column
	 */
	private int size;
	/**
	 * digits of the column
	 */
	private int decimalDigits;
	/**
	 * True if the column is nullable
	 */
	private boolean nullable;
	/**
	 * True if the column is indexed
	 */
	private boolean indexed;
	/**
	 * True if the column is unique
	 */
	private boolean unique;
	/**
	 * Null if the DB reports no default value
	 */
	private String defaultValue;
	/**
	 * True if the column is a primary key
	 */
	private boolean primaryKey;
	/**
	 * True if the column is a foreign key
	 */
	private boolean foreignKey;
	/**
	 * 每列的注释
	 */
	private String comment = "";

	public Column() {
	}

	public Column(Table table) {
		this.table = table;
	}

	/**
	 * Describe what the DbColumn constructor does
	 *
	 * @param table         Describe what the parameter does
	 * @param sqlType       Describe what the parameter does
	 * @param sqlTypeName   Describe what the parameter does
	 * @param sqlName       Describe what the parameter does
	 * @param size          Describe what the parameter does
	 * @param decimalDigits Describe what the parameter does
	 * @param isPk          Describe what the parameter does
	 * @param isNullable    Describe what the parameter does
	 * @param isIndexed     Describe what the parameter does
	 * @param defaultValue  Describe what the parameter does
	 * @param isUnique      Describe what the parameter does
	 */
	public Column(Table table, int sqlType, String sqlTypeName, String sqlName,
	              int size, int decimalDigits, boolean isPk, boolean isNullable,
	              boolean isIndexed, boolean isUnique, String defaultValue) {
		this.table = table;
		this.sqlType = sqlType;
		this.sqlName = sqlName;
		this.sqlTypeName = sqlTypeName;
		this.size = size;
		this.decimalDigits = decimalDigits;
		primaryKey = isPk;
		nullable = isNullable;
		indexed = isIndexed;
		unique = isUnique;
		this.defaultValue = defaultValue;
		log.debug(sqlName + " isPk -> " + primaryKey);
	}
}
