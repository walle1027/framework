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
		SystemContext systemContext = new SystemContext();
		headers.forEach((key, value) -> {
			if (value == null || value.isEmpty()) {
				return;
			}
			String headerValue = value.get(0);
			if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				systemContext.setUserToken(headerValue);

			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_ACCOUNT_ID, key)) {
				systemContext.setAccountId(headerValue);

			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_ACCOUNT_NAME, key)) {
				try {
					systemContext.setAccountName(URLDecoder.decode(headerValue, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					log.error("error decode accountName:" + headerValue + " caused by:" + e.getMessage(), e);
				}
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_ID, key)) {
				systemContext.setUserId(headerValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_NAME, key)) {
				try {
					systemContext.setUserName(URLDecoder.decode(headerValue, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					log.error("error decode accountName:" + headerValue + " caused by:" + e.getMessage(), e);
				}
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_CLIENT_IP, key)) {
				systemContext.setClientIp(headerValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_AGENT, key)) {
				systemContext.setUserAgent(headerValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_TENANT_CODE, key)) {
				systemContext.setTenantCode(headerValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_APP_ID, key)) {
				systemContext.setAppId(headerValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_APP_VERSION, key)) {
				systemContext.setAppVersion(headerValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_LOCALE, key)) {
				systemContext.setLocale(headerValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_TIME_ZONE, key)) {
				systemContext.setTimeZone(Integer.parseInt(headerValue));
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_SERVER_HOST, key)) {
				systemContext.setServerHost(headerValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_PLATFORM, key)) {
				systemContext.setPlatform(headerValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_DEVICE, key)) {
				systemContext.setDevice(headerValue);
			} else if (StringUtils.startsWithIgnoreCase(key, SystemContext.CONTEXT_PREFIX)) {
				systemContext.set(key.toLowerCase(), headerValue);
			}
		});

		return chain.filter(exchange).subscriberContext(context -> {
			return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext);
		});
	}
}
