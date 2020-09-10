package org.loed.framework.common.web.flux;

import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/15 2:44 下午
 */
public class ReactiveRedisI18nProvider implements ReactiveI18nProvider {

	private ReactiveStringRedisTemplate redisTemplate;

	@Override
	public Mono<String> getText(String key, Object[] args, String locale) {
		return ReactiveSystemContext.getSystemContext().map(context -> {
			return buildKey(context.getTenantId(), key, locale);
		}).flatMap(cacheKey -> {
			return redisTemplate.opsForValue().get(cacheKey);
		}).switchIfEmpty(redisTemplate.opsForValue().get(buildKey(SystemContext.DEFAULT_TENANT_ID, key, locale)))
				.defaultIfEmpty(key).map(value -> {
					return MessageFormat.format(value, args);
				});
	}

	/**
	 * 自动构建i18nkey
	 *
	 * @param key    调用的key
	 * @param locale 区域和语言
	 * @return 构建后的key
	 */
	private String buildKey(String tenantCode, String key, String locale) {
		return tenantCode + ":" +
				key +
				":" +
				locale;
	}

	public ReactiveStringRedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(ReactiveStringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
}
