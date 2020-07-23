package org.loed.framework.common.database;

import javax.persistence.GenerationType;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/10/9 上午9:23
 */
public class Column {
	/**
	 * 关联的表
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
	 * 数据库列名称
	 */
	private String sqlName;
	/**
	 * 长度
	 */
	private int length;
	/**
	 * 精度
	 */
	private int precision;
	/**
	 * 刻度
	 */
	private int scale;
	/**
	 * 是否为空
	 */
	private boolean nullable;
	/**
	 * 是否是索引
	 */
	private boolean indexed;
	/**
	 * 是否是唯一索引
	 */
	private boolean unique;
	/**
	 * 是否版本列
	 */
	private boolean versioned;
	/**
	 * 是否可以新增
	 */
	private boolean insertable = true;
	/**
	 * 是否可以修改
	 */
	private boolean updatable = true;
	/**
	 * 默认值
	 */
	private String defaultValue;
	/**
	 * 是否是主键
	 */
	private boolean isPk;
	/**
	 * 主键生成方式
	 */
	private GenerationType idGenerationType;
	/**
	 * 列注释
	 */
	private String sqlComment;
	/**
	 * java类型
	 */
	private Class<?> javaType;
	/**
	 * java名称
	 */
	private String javaName;
	/**
	 * columnDefinition
	 */
	private String columnDefinition;
	/**
	 * 是否分表列
	 */
	private boolean shardingColumn;

	public Column(Table table) {
		this.table = table;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}

	public String getSqlTypeName() {
		return sqlTypeName;
	}

	public void setSqlTypeName(String sqlTypeName) {
		this.sqlTypeName = sqlTypeName;
	}

	public String getSqlName() {
		return sqlName;
	}

	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isPk() {
		return isPk;
	}

	public void setPk(boolean pk) {
		isPk = pk;
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	public void setJavaType(Class<?> javaType) {
		this.javaType = javaType;
	}

	public String getJavaName() {
		return javaName;
	}

	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}

	public String getSqlComment() {
		return sqlComment;
	}

	public void setSqlComment(String sqlComment) {
		this.sqlComment = sqlComment;
	}

	public String getColumnDefinition() {
		return columnDefinition;
	}

	public void setColumnDefinition(String columnDefinition) {
		this.columnDefinition = columnDefinition;
	}

	public boolean isShardingColumn() {
		return shardingColumn;
	}

	public void setShardingColumn(boolean shardingColumn) {
		this.shardingColumn = shardingColumn;
	}

	public boolean isInsertable() {
		return insertable;
	}

	public void setInsertable(boolean insertable) {
		this.insertable = insertable;
	}

	public boolean isUpdatable() {
		return updatable;
	}

	public void setUpdatable(boolean updatable) {
		this.updatable = updatable;
	}

	public boolean isVersioned() {
		return versioned;
	}

	public void setVersioned(boolean versioned) {
		this.versioned = versioned;
	}

	public GenerationType getIdGenerationType() {
		return idGenerationType;
	}

	public void setIdGenerationType(GenerationType idGenerationType) {
		this.idGenerationType = idGenerationType;
	}
}
