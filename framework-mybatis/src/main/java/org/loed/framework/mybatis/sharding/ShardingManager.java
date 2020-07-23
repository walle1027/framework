package org.loed.framework.mybatis.sharding;


import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.consistenthash.ConsistentHash;
import org.loed.framework.common.database.Table;
import org.loed.framework.mybatis.sharding.table.TableConsistentHash;
import org.loed.framework.mybatis.sharding.table.TableNode;
import org.loed.framework.mybatis.sharding.table.po.IdMapping;
import org.loed.framework.mybatis.sharding.table.po.ShardingMapping;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/27 下午4:35
 */
public interface ShardingManager {
	/**
	 * 保存idMapping 信息
	 *
	 * @param shardingMappings 保存shardingValue 和 shardingTable的映射
	 * @return 记录数量
	 */
	int saveShardingMappings(ShardingMapping... shardingMappings);

	/**
	 * 保存主键和shardingValue的映射
	 *
	 * @param idMappings idMappings
	 * @return 记录数量
	 */
	int saveIdMappings(IdMapping... idMappings);

	/**
	 * 获取sharing信息
	 *
	 * @param table         表元数据描述信息
	 * @param shardingValue 分表键对应的值
	 * @return 分片配置信息
	 */
	String getShardingTableNameByValue(Table table, String shardingValue);

	/**
	 * 根据主键获取分表的名称
	 *
	 * @param table 表元数据描述信息
	 * @param id    主键
	 * @return 分表名称
	 */
	String getShardingTableNameById(Table table, Serializable id);

	/**
	 * 批量获取
	 *
	 * @param table          表元数据描述信息
	 * @param shardingValues 多个分表键对应的值
	 * @return 分表名称键值对
	 */
	Map<String, String> getShardingTableNameByValues(Table table, Set<String> shardingValues);

	/**
	 * 批量获取
	 *
	 * @param table 表元数据描述信息
	 * @param ids   多个Id
	 * @return 分表名称键值对
	 */
	Map<Serializable, String> getShardingTableNameByIds(Table table, Set<Serializable> ids);

	/**
	 * 利用一致性hash算法进行分表
	 *
	 * @param table         表名对象配置
	 * @param shardingValue 分表键的值
	 * @return 分表后的表名
	 */
	default String shardingTable(Table table, String shardingValue) {
		//一致hash分表
		ConsistentHash<TableNode> consistentHash;
		if (StringUtils.isNotBlank(table.getShardingAlias())) {
			consistentHash = TableConsistentHash.getOrCreate(table.getShardingAlias(), table.getShardingCount());
		} else {
			consistentHash = TableConsistentHash.getOrCreate(table.getSqlName(), table.getShardingCount());
		}
		TableNode tableNode = consistentHash.get(shardingValue);
		return table.getSqlName() + "_" + tableNode.getIndex();
	}
}
