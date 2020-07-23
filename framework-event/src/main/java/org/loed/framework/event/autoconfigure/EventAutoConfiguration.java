package org.loed.framework.event.autoconfigure;

import org.loed.framework.common.ConfigureConstant;
import org.loed.framework.common.PoolExecutors;
import org.loed.framework.common.ThreadPoolExecutor;
import org.loed.framework.event.EventManager;
import org.loed.framework.event.support.disruptor.DisruptorEventManager;
import org.loed.framework.event.support.redis.RedisEventManager;
import org.loed.framework.event.support.redis.RedisEventProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/13 下午2:45
 */
@Configuration
public class EventAutoConfiguration {

	@Bean("eventManager")
	public EventManager disruptorEventManager() {
		return new DisruptorEventManager();
	}

	@Bean("redisEventManager")
	@Primary
	@ConditionalOnBean(StringRedisTemplate.class)
	public EventManager redisEventManager(StringRedisTemplate redisTemplate) {
		RedisEventManager redisEventManager = new RedisEventManager();
		redisEventManager.setRedisTemplate(redisTemplate);
		return redisEventManager;
	}

	@Bean
	@ConditionalOnBean(StringRedisTemplate.class)
	public RedisEventProcessor redisEventProcessor(StringRedisTemplate redisTemplate) {
		RedisEventProcessor redisEventProcessor = new RedisEventProcessor();
		redisEventProcessor.setRedisTemplate(redisTemplate);
		ThreadPoolExecutor poolExecutor = PoolExecutors.newThreadPool(ConfigureConstant.event_region_name, 5, 10, 10);
		redisEventProcessor.setTaskExecutor(poolExecutor);
		return redisEventProcessor;
	}
}
