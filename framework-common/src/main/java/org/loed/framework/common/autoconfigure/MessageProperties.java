package org.loed.framework.common.autoconfigure;

import org.loed.framework.common.ConfigureConstant;
import org.loed.framework.common.web.validate.MessageProviderType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/23 上午11:50
 */
@ConfigurationProperties(prefix = ConfigureConstant.message_ns)
public class MessageProperties {
	private MessageProviderType provider = MessageProviderType.redis;

	public MessageProviderType getProvider() {
		return provider;
	}

	public void setProvider(MessageProviderType provider) {
		this.provider = provider;
	}
}
