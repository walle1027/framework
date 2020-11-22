package org.loed.framework.common.autoconfigure;

import org.loed.framework.common.web.mvc.i18n.I18nProvider;
import org.loed.framework.common.web.mvc.i18n.RedisI18nProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/17 10:39 上午
 */
@ConditionalOnClass(RedisOperations.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@Configuration
public class RedisI18nProviderAutoConfiguration {
	@Bean
	@ConditionalOnBean(StringRedisTemplate.class)
	public I18nProvider redisI18nProvider(StringRedisTemplate redisTemplate) {
		RedisI18nProvider redisI18nProvider = new RedisI18nProvider();
		redisI18nProvider.setRedisTemplate(redisTemplate);
		return redisI18nProvider;
	}
}
