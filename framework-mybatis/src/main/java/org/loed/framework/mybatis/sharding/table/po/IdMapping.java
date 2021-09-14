package org.loed.framework.mybatis.sharding.table.po;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/10/2 上午10:19
 */
@Table(name = "t_id_mapping", indexes = {@Index(name = "idx_id_mapping", columnList = "table_id,table_name", unique = true)})
public class IdMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false, updatable = false)
	private BigInteger id;
	/**
	 * 原始数据的主键
	 */
	@Column(columnDefinition = "varchar(255)")
	private Serializable tableId;
	/**
	 * 表名
	 */
	@Column()
	private String tableName;
	/**
	 * 分表键
	 */
	@Column()
	private String shardingKey;
	/**
	 * 分表键对应的值
	 */
	@Column()
	private String shardingValue;
	/**
	 * 分表的名称
	 */
	@Column()
	private String shardingTableName;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof IdMapping)) {
			return false;
		}
		IdMapping idMapping = (IdMapping) o;
		return Objects.equals(tableId, idMapping.tableId) &&
				Objects.equals(shardingTableName, idMapping.shardingTableName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tableId, shardingTableName);
	}

	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}

	public Serializable getTableId() {
		return tableId;
	}

	public void setTableId(Serializable tableId) {
		this.tableId = tableId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getShardingKey() {
		return shardingKey;
	}

	public void setShardingKey(String shardingKey) {
		this.shardingKey = shardingKey;
	}

	public String getShardingValue() {
		return shardingValue;
	}

	public void setShardingValue(String shardingValue) {
		this.shardingValue = shardingValue;
	}

	public String getShardingTableName() {
		return shardingTableName;
	}

	public void setShardingTableName(String shardingTableName) {
		this.shardingTableName = shardingTableName;
	}
}
