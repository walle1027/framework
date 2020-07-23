package org.loed.framework.mybatis.sharding.table.po;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/27 下午4:42
 */
@Table(name = "t_sharding_mapping")
public class ShardingMapping {
	/**
	 * 原表的名称
	 */
	@Column
	private String tableName;
	/**
	 * 分表键
	 */
	@Column
	private String shardingKey;
	/**
	 * 分表值
	 */
	@Column
	private String shardingValue;
	/**
	 * 分表的名称
	 */
	@Column
	private String shardingTableName;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ShardingMapping)) return false;
		ShardingMapping that = (ShardingMapping) o;
		return Objects.equals(tableName, that.tableName) &&
				Objects.equals(shardingKey, that.shardingKey) &&
				Objects.equals(shardingValue, that.shardingValue) &&
				Objects.equals(shardingTableName, that.shardingTableName);
	}

	@Override
	public int hashCode() {

		return Objects.hash(tableName, shardingKey, shardingValue, shardingTableName);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getShardingTableName() {
		return shardingTableName;
	}

	public void setShardingTableName(String shardingTableName) {
		this.shardingTableName = shardingTableName;
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
}
