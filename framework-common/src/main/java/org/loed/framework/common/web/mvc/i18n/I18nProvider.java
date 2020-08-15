package org.loed.framework.common.web.mvc.i18n;

import org.loed.framework.common.context.SystemContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-2 下午11:56
 */

public interface I18nProvider {
	Logger logger = LoggerFactory.getLogger(I18nProvider.class);

	I18nProvider DEFAULT_I18N_PROVIDER = (key, args, locale) -> {
		if (key != null) {
			return String.format(key, args);
		} else {
			return null;
		}
	};

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
}
