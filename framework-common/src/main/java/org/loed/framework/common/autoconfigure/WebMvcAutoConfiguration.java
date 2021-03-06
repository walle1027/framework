package org.loed.framework.common.autoconfigure;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.web.mvc.DefaultExceptionHandler;
import org.loed.framework.common.web.mvc.ResponseBodyWrapFactoryBean;
import org.loed.framework.common.web.mvc.filter.SystemContextFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/3/24 10:01 AM
 */
@ConditionalOnBean(DispatcherServletAutoConfiguration.class)
@Configuration
public class WebMvcAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public DefaultExceptionHandler defaultExceptionHandler() {
		return new DefaultExceptionHandler();
	}

	@Bean
	@Conditional({ResponseBodyWrapperCondition.class})
	public ResponseBodyWrapFactoryBean responseBodyWrapper() {
		return new ResponseBodyWrapFactoryBean();
	}

	@Bean
	public RestTemplateCustomizer systemContextRestTemplateCustomizer() {
		return restTemplate -> restTemplate.getInterceptors().add((request, body, execution) -> {
			HttpHeaders headers = request.getHeaders();
			SystemContext systemContext = SystemContextHolder.getSystemContext();
			List<Pair<String, String>> pairs = systemContext.toHeaders();
			if (CollectionUtils.isNotEmpty(pairs)) {
				for (Pair<String, String> pair : pairs) {
					headers.add(pair.getLeft(), pair.getRight());
				}
			}
			return execution.execute(request, body);
		});
	}

	@Bean
	public FilterRegistrationBean<SystemContextFilter> systemContextFilterRegistration() {
		SystemContextFilter contextFilter = new SystemContextFilter();
		FilterRegistrationBean<SystemContextFilter> registration = new FilterRegistrationBean<>(contextFilter);
		registration.addUrlPatterns("*");
		registration.setOrder(contextFilter.getOrder());
		return registration;
	}
}
