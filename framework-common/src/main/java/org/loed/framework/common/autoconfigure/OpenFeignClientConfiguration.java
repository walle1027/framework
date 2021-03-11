package org.loed.framework.common.autoconfigure;

import feign.RequestInterceptor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.context.SystemContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * openfeign {@link SystemContext}  support
 *
 * @author thomason
 * @version 1.0
 * @since 2021/3/9 2:39 下午
 */
@Configuration
@ConditionalOnBean(name = "org.springframework.cloud.openfeign.FeignClientsRegistrar")
public class OpenFeignClientConfiguration {
	@Bean
	public RequestInterceptor systemContextInterceptor() {
		return requestTemplate -> {
			SystemContext systemContext = SystemContextHolder.getSystemContext();
			List<Pair<String, String>> pairs = systemContext.toHeaders();
			if (CollectionUtils.isNotEmpty(pairs)) {
				for (Pair<String, String> pair : pairs) {
					requestTemplate.header(pair.getLeft(), pair.getRight());
				}
			}
		};
	}
}
