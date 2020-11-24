package org.loed.framework.mybatis.autoconfigure;

import org.apache.ibatis.session.SqlSessionFactory;
import org.loed.framework.common.ThreadPoolExecutor;
import org.loed.framework.common.autoconfigure.DbInspectorRegister;
import org.loed.framework.mybatis.inspector.autoconfigure.DbInspector;
import org.loed.framework.mybatis.sharding.ShardingManager;
import org.loed.framework.mybatis.sharding.table.impl.DoubleWriteManager;
import org.loed.framework.mybatis.sharding.table.impl.JdbcShardingManager;
import org.loed.framework.mybatis.sharding.table.impl.RedisShardingManager;
import org.loed.framework.mybatis.sharding.table.po.ShardingMapping;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/27 9:24 上午
 */
@Configuration
public class ShardingAutoConfiguration {
	@Bean
	public ShardingManager jdbcShardingManager(SqlSessionFactory sqlSessionFactory) {
		JdbcShardingManager jdbcShardingManager = new JdbcShardingManager();
		jdbcShardingManager.setSqlSessionFactory(sqlSessionFactory);
		return jdbcShardingManager;
	}

	@Bean
	public ShardingManager redisShardingManager(StringRedisTemplate redisTemplate) {
		return new RedisShardingManager(redisTemplate);
	}

	@Bean
	@Primary
	public ShardingManager shardingManager(ThreadPoolExecutor threadPoolExecutor,
	                                       @Qualifier("jdbcShardingManager") ShardingManager jdbcShardingManager
			, @Qualifier("redisShardingManager") ShardingManager redisShardingManager) {
		DbInspectorRegister.addPackages(ShardingMapping.class);
		DoubleWriteManager manager = new DoubleWriteManager();
		manager.setJdbcShardingManager(jdbcShardingManager);
		manager.setRedisShardingManager(redisShardingManager);
		manager.setPoolExecutor(threadPoolExecutor);
		return manager;
	}
}
