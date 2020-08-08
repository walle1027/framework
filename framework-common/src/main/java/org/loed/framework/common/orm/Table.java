package org.loed.framework.common.orm;

import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import javax.persistence.GenerationType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/10/9 上午9:21
 */
@Data
@ToString
public class Table {
	/**
	 * db schema
	 */
	private String schema;
	/**
	 * db catalog
	 */
	private String catalog;
	/**
	 * 表名称
	 */
	private String sqlName;
	/**
	 * 表注释
	 */
	private String comment;
	/**
	 * oracle 数据owner
	 */
	private String ownerSynonymName;
	/**
	 * 列
	 */
	private List<Column> columns;
	/**
	 * 索引
	 */
	private List<Index> indices;
	/**
	 * 版本列
	 */
	private Column versionColumn;
	/**
	 * 表对应的java对象简名称
	 */
	private String simpleJavaName;
	/**
	 * java对象的全名
	 */
	private String javaName;
	/**
	 * 数据库连接信息
	 */
	private Database database;
	/**
	 * 是否分表
	 */
	private boolean sharding;
	/**
	 * 分表别名
	 */
	private String shardingAlias;
	/**
	 * 分表个数
	 */
	private int shardingCount;
	/**
	 * 此对象包含的一对一的关联对象
	 */
	private List<Join> joins;
	/**
	 * 主键生成方式
	 */
	private GenerationType idGenerationType = GenerationType.IDENTITY;

	public void addIndex(Index index) {
		if (this.indices == null) {
			this.indices = new ArrayList<>();
		}
		this.indices.add(index);
	}

	public void addColumn(Column column) {
		if (this.columns == null) {
			this.columns = new ArrayList<>();
		}
		this.columns.add(column);
	}

	public void addJoin(Join join) {
		if (this.joins == null) {
			this.joins = new ArrayList<>();
		}
		this.joins.add(join);
	}

	public boolean hasVersionColumn() {
		if (CollectionUtils.isEmpty(columns)) {
			return false;
		}
		return columns.stream().anyMatch(Column::isVersioned);
	}
}
