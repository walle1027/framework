package org.loed.framework.common.i18n;

import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.i18n.I18nProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-3 上午12:02
 */
public class RedisI18nProvider implements I18nProvider {

	private StringRedisTemplate redisTemplate;

	/**
	 * 设置 线程变量
	 *
	 * @param map
	 */
	private void set(Map<String, String> map) {
		threadLocal.set(map);
	}

	public Map<String, String> get() {
		return threadLocal.get();
	}

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
		Map<String, String> i18nMap = get();
		if (i18nMap == null) {
			i18nMap = new HashMap<>();
			set(i18nMap);
		}
		if (i18nMap.containsKey(key)) {
			return i18nMap.get(key);
		} else {
			String value = redisTemplate.opsForValue().get(key);
			if (value != null) {
				i18nMap.put(key, value);
				return value;
			} else {
				return null;
			}
		}
	}

	/**
	 * 设置i18n值，同时设置到线程变量和redis中
	 *
	 * @param key   键
	 * @param value 值
	 */
	public void set(String key, String value) {
		Map<String, String> i18nMap = get();
		if (i18nMap == null) {
			i18nMap = new HashMap<String, String>();
			set(i18nMap);
		}
		i18nMap.put(key, value);
		redisTemplate.opsForValue().set(key, value);
	}

	/**
	 * 取得国际化值
	 * 默认取简体中文值
	 *
	 * @param key 键
	 * @return 值
	 */
	@Override
	public String getText(String key) {
		return getText(key, DEFAULT_LOCALE);
	}

	/**
	 * 取得国际化值
	 *
	 * @param key    键
	 * @param locale 区域
	 * @return 值
	 */
	@Override
	public String getText(String key, String locale) {
		String value = get(buildKey(key, locale));
		return value == null ? key : value;
	}

	/**
	 * 取得国际化值，并进行格式化
	 *
	 * @param key  键
	 * @param args 参数
	 * @return 值
	 */
	@Override
	public String getText(String key, Object[] args) {
		return getText(key, args, DEFAULT_LOCALE);
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
		String realKey = buildKey(key, locale);
		String value = get(realKey);
		if (value == null) {
			value = key;
		}
		return MessageFormat.format(value, args);
	}

	/**
	 * 自动构建i18nkey
	 *
	 * @param key    调用的key
	 * @param locale 区域和语言
	 * @return 构建后的key
	 */
	private String buildKey(String key, String locale) {
		StringBuilder builder = new StringBuilder();
		builder.append(SystemContext.getTenantCode());
		builder.append(I18N_KEY_SEPARATOR);
		builder.append(key);
		builder.append(I18N_KEY_SEPARATOR);
		builder.append(locale);
		return builder.toString();
	}

	public StringRedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
}
