package org.loed.framework.common.web.mvc.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.ServiceLocator;
import org.loed.framework.common.i18n.I18nProvider;
import org.loed.framework.common.web.freemarker.AbstractDirective;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;

/**
 * 获取国际化值的指令
 *
 * @author Thomason
 * @version 1.0
 * @since 11-9-12 下午10:52
 */
public class I18nDirective extends AbstractDirective {
	private String basepath;

	public I18nDirective() {
	}

	public I18nDirective(String basepath) {
		this.basepath = basepath;
	}

	@Override
	public void execute(Environment environment, Map map, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
		String key = getStringFromModel(map, "name");
		String params = getStringFromModel(map, "params");
		String[] paramArr = org.springframework.util.StringUtils.commaDelimitedListToStringArray(params);
		Assert.hasText(key);
		String locale = getStringFromEnvironment(environment, "locale");
		if (StringUtils.isEmpty(locale)) {
			locale = I18nProvider.DEFAULT_LOCALE;
		}
		String value = key;
		I18nProvider i18nProvider = getI18nProvider();
		if (i18nProvider != null) {
			if (paramArr.length > 0) {
				value = i18nProvider.getText(key, paramArr, locale);
			} else {
				value = i18nProvider.getText(key, locale);
			}
		}
		environment.getOut().write(value);
	}

	public I18nProvider getI18nProvider() {
		return ServiceLocator.getService(I18nProvider.class);
	}
}
