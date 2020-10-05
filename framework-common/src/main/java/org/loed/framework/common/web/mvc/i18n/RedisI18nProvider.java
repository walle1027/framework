package org.loed.framework.common.web.mvc.i18n;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.context.SystemContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-3 上午12:02
 */
@Slf4j
public class RedisI18nProvider implements I18nProvider {

	Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS)
			.maximumSize(10000).build();

	private StringRedisTemplate redisTemplate;

	/**
	 * 取出属性
	 * 先从线程变量中取，如果线程变量中不存在，
	 * 从redis中取，
	 * 取出后同时保存到线程变量中
	 *
	 * @param key 键
	 * @return 值
	 */
	public String get(String key) {
		try {
			return cache.get(key, () -> {
				return redisTemplate.opsForValue().get(key);
			});
		} catch (ExecutionException e) {
			log.error("can't find cache for key :" + key + " caused by :" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 取得国际化值，并进行格式化
	 *
	 * @param key    键
	 * @param args   参数
	 * @param locale 区域
	 * @return 值
	 */
	@Override
	public String getText(String key, Object[] args, String locale) {
		String value = get(buildKey(SystemContextHolder.getTenantId(), key, locale));
		if (value == null) {
			value = get(buildKey(SystemContext.DEFAULT_TENANT_ID, key, locale));
		}
		if (value == null) {
			value = key;
		}
		if (value != null) {
			return MessageFormat.format(value, args);
		}
		return null;
	}

	/**
	 * 自动构建i18nkey
	 *
	 * @param key    调用的key
	 * @param locale 区域和语言
	 * @return 构建后的key
	 */
	private String buildKey(String tenantCode, String key, String locale) {
		return tenantCode +
				I18N_KEY_SEPARATOR +
				key +
				I18N_KEY_SEPARATOR +
				locale;
	}

	public StringRedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
}
