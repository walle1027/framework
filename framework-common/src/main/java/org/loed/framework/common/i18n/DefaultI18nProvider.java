package org.loed.framework.common.i18n;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/25 1:05 PM
 */
public class DefaultI18nProvider implements I18nProvider {
	@Override
	public String getText(String key, Object[] args, String locale) {
		if (key != null) {
			return String.format(key, args);
		} else {
			return null;
		}

	}
}
