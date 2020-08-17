package org.loed.framework.common.autoconfigure;

import org.loed.framework.common.web.flux.ReactiveI18nProvider;
import org.loed.framework.common.web.flux.ReactiveRedisI18nProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Flux;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/17 10:36 上午
 */
@ConditionalOnClass({ReactiveRedisConnectionFactory.class, ReactiveRedisTemplate.class, Flux.class})
@Configuration
public class ReactiveI18nProviderAutoConfiguration {
	@Bean
	@ConditionalOnBean(ReactiveStringRedisTemplate.class)
	public ReactiveI18nProvider reactiveRedisI18nProvider(ReactiveStringRedisTemplate stringRedisTemplate) {
		ReactiveRedisI18nProvider reactiveRedisI18nProvider = new ReactiveRedisI18nProvider();
		reactiveRedisI18nProvider.setStringRedisTemplate(stringRedisTemplate);
		return reactiveRedisI18nProvider;
	}
}
