package org.loed.framework.common.autoconfigure;

import org.loed.framework.common.ConfigureConstant;
import org.loed.framework.common.web.mvc.ResponseBodyWrapFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/3/24 10:01 AM
 */
@ConditionalOnClass(DispatcherServlet.class)
@Configuration
public class WebMvcAutoConfiguration {
	@Bean
	@ConditionalOnProperty(prefix = ConfigureConstant.default_ns, name = "autoWrapResponse", havingValue = "true", matchIfMissing = true)
	public ResponseBodyWrapFactoryBean responseBodyWrapper() {
		return new ResponseBodyWrapFactoryBean();
	}
}
