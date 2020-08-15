package org.loed.framework.common.web.flux;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomason
 * @version 1.0
 * @since 2020/7/5 18:26
 */
@Slf4j
public class ReactiveSystemContextFilter implements WebFilter {
	@Override
	@NonNull
	public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
		HttpHeaders headers = exchange.getRequest().getHeaders();
		Map<String, String> contextMap = new HashMap<>();
		headers.forEach((key, value) -> {
			if (value == null || value.isEmpty()) {
				return;
			}
			String firstValue = value.get(0);
			if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);

			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_ACCOUNT_ID, key)) {
				contextMap.put(SystemContext.CONTEXT_ACCOUNT_ID, firstValue);

			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_ACCOUNT_NAME, key)) {
				try {
					contextMap.put(SystemContext.CONTEXT_ACCOUNT_NAME, URLDecoder.decode(firstValue, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					log.error("error decode accountName:" + firstValue + " caused by:" + e.getMessage(), e);
				}
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_ID, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_ID, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_NAME, key)) {
				try {
					contextMap.put(SystemContext.CONTEXT_USER_NAME, URLDecoder.decode(firstValue, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					log.error("error decode accountName:" + firstValue + " caused by:" + e.getMessage(), e);
				}
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_CLIENT_IP, key)) {
				contextMap.put(SystemContext.CONTEXT_CLIENT_IP, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_AGENT, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_AGENT, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_TENANT_CODE, key)) {
				contextMap.put(SystemContext.CONTEXT_TENANT_CODE, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_APP_ID, key)) {
				contextMap.put(SystemContext.CONTEXT_APP_ID, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_APP_VERSION, key)) {
				contextMap.put(SystemContext.CONTEXT_APP_VERSION, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_LOCALE, key)) {
				contextMap.put(SystemContext.CONTEXT_LOCALE, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_TIME_ZONE, key)) {
				contextMap.put(SystemContext.CONTEXT_TIME_ZONE, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_SERVER_HOST, key)) {
				contextMap.put(SystemContext.CONTEXT_SERVER_HOST, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_PLATFORM, key)) {
				contextMap.put(SystemContext.CONTEXT_PLATFORM, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_DEVICE, key)) {
				contextMap.put(SystemContext.CONTEXT_DEVICE, firstValue);
			} else if (StringUtils.startsWithIgnoreCase(key, SystemContext.CONTEXT_PREFIX)) {
				contextMap.put(key.toLowerCase(), firstValue);
			}
		});

		return chain.filter(exchange).subscriberContext(context -> {
			return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, contextMap);
		});
	}
}
