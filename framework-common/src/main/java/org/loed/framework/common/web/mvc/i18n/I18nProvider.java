package org.loed.framework.common.web.mvc.i18n;

import org.loed.framework.common.context.SystemContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-2 下午11:56
 */

public interface I18nProvider {
	Logger logger = LoggerFactory.getLogger(I18nProvider.class);
	ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<Map<String, String>>();
	/**
	 * 默认的区域
	 */
	String DEFAULT_LOCALE = "zh_CN";
	/**
	 * i18nKey分隔符
	 */
	String I18N_KEY_SEPARATOR = ":";

	/**
	 * 取得国际化值
	 * 默认取简体中文值
	 *
	 * @param key 键
	 * @return 值
	 */
	default String getText(String key) {
		return getText(key, null, SystemContextHolder.getLocale());
	}

	/**
	 * 取得国际化值
	 *
	 * @param key    键
	 * @param locale 区域
	 * @return 值
	 */
	default String getText(String key, String locale) {
		return getText(key, null, locale);
	}

	/**
	 * 取得国际化值，并进行格式化
	 *
	 * @param key  键
	 * @param args 参数
	 * @return 值
	 */
	default String getText(String key, Object[] args) {
		return getText(key, args, SystemContextHolder.getLocale());
	}

	/**
	 * 取得国际化值，并进行格式化
	 *
	 * @param key    键
	 * @param args   参数
	 * @param locale 区域
	 * @return 值
	 */
	String getText(String key, Object[] args, String locale);

//	/**
//	 * 设置I18nkey值
//	 *
//	 * @param key   键
//	 * @param value 值
//	 */
//	void setI18nValue(String key, String value);
//
//	/**
//	 * 设置I18nKey的值
//	 *
//	 * @param key    键
//	 * @param value  值
//	 * @param locale 区域
//	 */
//	void setI18nValue(String key, String value, String locale);
//
//	/**
//	 * 删除一个i18nKey
//	 *
//	 * @param key
//	 */
//	void removeI18nValue(String key);
//
//	/**
//	 * 删除一个i18nKey
//	 *
//	 * @param key
//	 * @param locale
//	 */
//	void removeI18nValue(String key, String locale);
//
//	/**
//	 * 取得所有的key
//	 *
//	 * @param locale 语言
//	 * @return 所有的国际化Key
//	 */
//	SortedMap<String, String> getAllKeys(String locale);
}
