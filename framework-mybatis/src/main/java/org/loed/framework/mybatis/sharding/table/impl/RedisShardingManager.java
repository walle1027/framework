package org.loed.framework.mybatis.sharding.table.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.database.Table;
import org.loed.framework.mybatis.sharding.ShardingManager;
import org.loed.framework.mybatis.sharding.table.po.IdMapping;
import org.loed.framework.mybatis.sharding.table.po.ShardingMapping;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/28 上午8:07
 */
public class RedisShardingManager implements ShardingManager {

	private StringRedisTemplate redisTemplate;

	@Override
	public int saveShardingMappings(ShardingMapping... shardingMappings) {
		if (shardingMappings == null || shardingMappings.length == 0) {
			return 0;
		}
		Set<ShardingMapping> collect = Arrays.stream(shardingMappings).collect(Collectors.toSet());
		AtomicInteger count = new AtomicInteger(0);
		collect.parallelStream().forEach(c -> {
			String cacheKey = buildShardingCacheKey(c.getTableName(), c.getShardingValue());
			Map<String, String> shardingMap = new HashMap<>();
			shardingMap.put("tableName", c.getTableName());
			shardingMap.put("shardingKey", c.getShardingKey());
			shardingMap.put("shardingValue", c.getShardingValue());
			shardingMap.put("shardingTableName", c.getShardingTableName() == null ? "" : c.getShardingTableName());

			redisTemplate.opsForHash().putAll(cacheKey, shardingMap);
			count.getAndAdd(1);
		});
		return count.get();
	}

	@Override
	public int saveIdMappings(IdMapping... idMappings) {
		if (idMappings == null || idMappings.length == 0) {
			return 0;
		}
		Set<IdMapping> collect = Arrays.stream(idMappings).collect(Collectors.toSet());
		AtomicInteger count = new AtomicInteger(0);
		collect.forEach(c -> {
			String cacheKey = buildIdCacheKey(c.getTableName(), c.getId());
			Map<String, String> shardingMap = new HashMap<>();
			shardingMap.put("id", c.getId() + "");
			shardingMap.put("tableName", c.getTableName());
			shardingMap.put("shardingKey", c.getShardingKey());
			shardingMap.put("shardingValue", c.getShardingValue());
			shardingMap.put("shardingTableName", c.getShardingTableName() == null ? "" : c.getShardingTableName());
			redisTemplate.opsForHash().putAll(cacheKey, shardingMap);
			count.getAndAdd(1);
		});
		return count.get();
	}

	private String buildShardingCacheKey(String tableName, String shardingValue) {
		return "sharding:valueMapping:" + tableName + ":" + shardingValue;
	}

	private String buildIdCacheKey(String tableName, Serializable id) {
		return "sharding:idMapping:" + tableName + ":" + id;
	}

	@Override
	public String getShardingTableNameByValue(Table table, String shardingValue) {
		String cacheKey = buildShardingCacheKey(table.getSqlName(), shardingValue);
		return (String) redisTemplate.opsForHash().get(cacheKey, "shardingTableName");
	}

	@Override
	public String getShardingTableNameById(Table table, Serializable id) {
		String idCacheKey = buildIdCacheKey(table.getSqlName(), id);
		return (String) redisTemplate.opsForHash().get(idCacheKey, "shardingTableName");
	}

	@Override
	public Map<String, String> getShardingTableNameByValues(Table table, Set<String> shardingValues) {
		Map<String, String> map = new HashMap<>();
		if (CollectionUtils.isEmpty(shardingValues)) {
			return map;
		}
		for (String shardingValue : shardingValues) {
			String shardingTableName = getShardingTableNameByValue(table, shardingValue);
			if (StringUtils.isNotBlank(shardingTableName)) {
				map.put(shardingValue, shardingTableName);
			}
		}
		return map;
	}

	@Override
	public Map<Serializable, String> getShardingTableNameByIds(Table table, Set<Serializable> ids) {
		Map<Serializable, String> map = new HashMap<>();
		if (CollectionUtils.isEmpty(ids)) {
			return map;
		}
		for (Serializable id : ids) {
			String shardingTableName = getShardingTableNameById(table, id);
			if (StringUtils.isNotBlank(shardingTableName)) {
				map.put(id, shardingTableName);
			}
		}
		return map;
	}

	public StringRedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
}
