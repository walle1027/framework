package org.loed.framework.common.autoconfigure;

import org.loed.framework.common.web.validate.MessageProvider;
import org.loed.framework.common.web.validate.MessageProviderType;
import org.loed.framework.common.web.validate.message.PlainMessageInterpolator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/23 上午11:37
 */
@Configuration
@EnableConfigurationProperties({MessageProperties.class})
public class ValidatorAutoConfiguration {
	@Autowired
	private MessageProperties messageProperties;

	@Bean
	MessageProvider messageProvider() {
		MessageProviderType provider = messageProperties.getProvider();
		MessageProvider messageProvider = new MessageProvider();
		messageProvider.setProviderType(provider);
		return messageProvider;
	}

	@Bean
	public Validator getValidator(MessageProvider messageProvider) throws Exception {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		PlainMessageInterpolator messageInterpolator = new PlainMessageInterpolator();
		messageInterpolator.setMessageProvider(messageProvider);
		validator.setMessageInterpolator(messageInterpolator);
		return validator;
	}
}
