package org.loed.framework.common.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.context.SystemContextExchangeFilter;
import org.loed.framework.common.web.flux.DefaultExceptionHandler;
import org.loed.framework.common.web.flux.ReactiveSystemContextFilter;
import org.loed.framework.common.web.flux.codec.JsonResultWrapperEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.server.WebFilter;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/11 7:53 下午
 */
@Slf4j
@ConditionalOnBean(WebFluxConfigurationSupport.class)
@Configuration
public class WebFluxAutoConfiguration {

	@Bean
	public DefaultExceptionHandler defaultExceptionHandler() {
		return new DefaultExceptionHandler();
	}

	@Bean
	@Order()
	public WebFilter reactiveSystemContextFilter() {
		return new ReactiveSystemContextFilter();
	}

	@Bean
	public WebClientCustomizer systemContextCustomizer() {
		return builder -> {
			builder.filter(systemContextExchangeFilter());
		};
	}

	@Bean
	public ExchangeFilterFunction systemContextExchangeFilter() {
		return new SystemContextExchangeFilter();
	}

	@Bean
	@Conditional(ResponseBodyWrapperCondition.class)
	@Order(Ordered.LOWEST_PRECEDENCE - 1)
	public CodecCustomizer resultWrapperEncoder(ObjectProvider<ObjectMapper> mapperObjectProvider) {
		ObjectMapper objectMapper = mapperObjectProvider.getIfAvailable();
		return configurer -> {
			if (objectMapper != null) {
				configurer.customCodecs().register(new JsonResultWrapperEncoder(objectMapper));
			} else {
				configurer.customCodecs().register(new JsonResultWrapperEncoder());
			}
		};
	}
}
