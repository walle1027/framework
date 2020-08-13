package org.loed.framework.common.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.web.flux.ReactiveSystemContextFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;

import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/11 7:53 下午
 */
@ConditionalOnBean(WebFluxConfigurationSupport.class)
@Configuration
@Slf4j
public class WebFluxAutoConfiguration {

	@Bean
	public ReactiveSystemContextFilter reactiveSystemContextFilter() {
		return new ReactiveSystemContextFilter();
	}

	@Bean
	public WebClientCustomizer systemContextWebClientCustomizer() {
		return builder -> {
			builder.filter((request, next) -> ReactiveSystemContext.getSystemContext().flatMap(context -> {
				HttpHeaders headers = request.headers();
				List<Pair<String, String>> pairs = context.toHeaders();
				if (!pairs.isEmpty()) {
					for (Pair<String, String> pair : pairs) {
						headers.add(pair.getKey(), pair.getValue());
					}
				}
				return next.exchange(request);
			}));
		};
	}
}
