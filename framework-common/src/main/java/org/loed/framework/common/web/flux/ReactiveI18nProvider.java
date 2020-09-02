package org.loed.framework.common.web.flux;

import org.loed.framework.common.context.ReactiveSystemContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/13 4:17 下午
 */
public interface ReactiveI18nProvider {
	Logger logger = LoggerFactory.getLogger(ReactiveI18nProvider.class);


	ReactiveI18nProvider DEFAULT_REACTIVE_I18N_PROVIDER = (key, args, locale) -> {
		if (key == null) {
			return Mono.empty();
		}
		return Mono.just(MessageFormat.format(key, args));
	};

	/**
	 * 取得国际化值
	 * 默认取简体中文值
	 *
	 * @param key 键
	 * @return 值
	 */
	default Mono<String> getText(String key) {
		return ReactiveSystemContext.getSystemContext().flatMap(context -> getText(key, null, context.getLocale()));
	}

	/**
	 * 取得国际化值，并进行格式化
	 *
	 * @param key  键
	 * @param args 参数
	 * @return 值
	 */
	default Mono<String> getText(String key, Object[] args) {
		return ReactiveSystemContext.getSystemContext().flatMap(context -> getText(key, args, context.getLocale()));
	}

	/**
	 * 取得国际化值，并进行格式化
	 *
	 * @param key    键
	 * @param args   参数
	 * @param locale 区域
	 * @return 值
	 */
	Mono<String> getText(String key, Object[] args, String locale);
}
