package org.loed.framework.mybatis.sharding.table.po;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/10/2 上午10:19
 */
@Table(name = "t_id_mapping")
public class IdMapping {
	/**
	 * 原始数据的主键
	 */
	@Column()
	private Serializable id;
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
		return Objects.equals(id, idMapping.id) &&
				Objects.equals(shardingTableName, idMapping.shardingTableName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, shardingTableName);
	}

	public Serializable getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = id;
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
