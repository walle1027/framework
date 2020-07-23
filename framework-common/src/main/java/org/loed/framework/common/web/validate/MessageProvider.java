package org.loed.framework.common.web.validate;

import org.loed.framework.common.ServiceLocator;
import org.loed.framework.common.i18n.I18nProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/10/13 17:06
 */

public class MessageProvider {
	private static Logger logger = LoggerFactory.getLogger(MessageProvider.class);

	private MessageProviderType providerType;

	/**
	 * 获取消息文字
	 *
	 * @param key 消息key
	 * @return 消息文本
	 */
	public String getText(String key) {
		return getText(null, key, null);
	}

	/**
	 * 获取消息文字
	 *
	 * @param key  消息key
	 * @param args 消息参数
	 * @return 消息文本
	 */
	public String getText(String key, Object[] args) {
		return getText(null, key, args);
	}

	/**
	 * 获取消息文字
	 *
	 * @param textProvider 当使用属性文件做为i18n资源时，属性文件的提供者(由struts和springwebmvc 分别实现)
	 * @param key          消息key
	 * @param args         消息参数
	 * @return 消息文本
	 */
	public String getText(TextProvider textProvider, String key, Object[] args) {
		try {
			switch (providerType) {
				case redis:
					I18nProvider i18nService = i18nService();
					return i18nService.getText(key, args);
				case properties:
					return textProvider.getText(key, args);
			}
		} catch (Throwable e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		}
		return key;
	}

	public MessageProviderType getProviderType() {
		return providerType;
	}

	public void setProviderType(MessageProviderType providerType) {
		this.providerType = providerType;
	}

	/**
	 * 取国际化资源的服务类
	 *
	 * @return 国际化资源的服务类
	 */
	private I18nProvider i18nService() {
		return ServiceLocator.getService(I18nProvider.class);
	}

	public interface TextProvider {
		String getText(String key, Object[] args);
	}
}
