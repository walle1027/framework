package org.loed.framework.common.autoconfigure;

import com.rabbitmq.client.Channel;
import org.loed.framework.common.rabbit.SystemContextAwareMessageConverter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/10/20 11:59 上午
 */
@ConditionalOnClass({RabbitTemplate.class, Channel.class})
@Configuration
public class SystemContextRabbitAutoConfiguration {
	@Bean
	@ConditionalOnClass(RabbitTemplate.class)
	public MessageConverter systemContextAwareMessageConverter() {
		SimpleMessageConverter simpleMessageConverter = new SimpleMessageConverter();
		return new SystemContextAwareMessageConverter(simpleMessageConverter);
	}
}
