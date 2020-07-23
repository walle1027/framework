package org.loed.framework.common.autoconfigure;

import org.loed.framework.common.web.mvc.filter.SystemContextFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/7 1:17 PM
 */
@Configuration
@EnableConfigurationProperties({CommonProperties.class})
public class SystemContextFilterAutoConfiguration {
	@Autowired
	private CommonProperties properties;

	@Bean
	public FilterRegistrationBean systemContextFilterRegistration() {
		SystemContextFilter contextFilter = new SystemContextFilter();
		FilterRegistrationBean registration = new FilterRegistrationBean(contextFilter);
		registration.addUrlPatterns(properties.getSystemContextFilterPattern());
		registration.setOrder(-1);
		return registration;
	}
}
