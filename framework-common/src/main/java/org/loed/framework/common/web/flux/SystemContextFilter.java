package org.loed.framework.common.web.flux;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.springframework.http.HttpHeaders;
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
public class SystemContextFilter implements WebFilter {
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
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
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.equalsIgnoreCase(SystemContext.CONTEXT_USER_TOKEN, key)) {
				contextMap.put(SystemContext.CONTEXT_USER_TOKEN, firstValue);
			} else if (StringUtils.startsWithIgnoreCase(key, SystemContext.CONTEXT_PREFIX)) {
				contextMap.put(key.toLowerCase(), firstValue);
			}
		});

		String token = headers.getFirst(SystemContext.CONTEXT_USER_TOKEN);
		if (StringUtils.isNotBlank(token)) {
			contextMap.put(SystemContext.CONTEXT_USER_TOKEN, token);
		}

		String accountId = headers.getFirst(SystemContext.CONTEXT_ACCOUNT_ID);
		if (StringUtils.isNotBlank(accountId)) {
			contextMap.put(SystemContext.CONTEXT_ACCOUNT_ID, accountId);
		}

		String accountName = headers.getFirst(SystemContext.CONTEXT_ACCOUNT_NAME);
		if (StringUtils.isNotBlank(accountName)) {
			try {
				contextMap.put(SystemContext.CONTEXT_ACCOUNT_NAME, URLDecoder.decode(accountName, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				log.error("error decode accountName:" + accountName + " caused by:" + e.getMessage(), e);
			}
		}

		String userId = headers.getFirst(SystemContext.CONTEXT_USER_ID);
		if (StringUtils.isNotBlank(userId)) {
			contextMap.put(SystemContext.CONTEXT_USER_ID, userId);
		}

		String userName = headers.getFirst(SystemContext.CONTEXT_USER_NAME);
		if (StringUtils.isNotBlank(userName)) {
			try {
				contextMap.put(SystemContext.CONTEXT_USER_NAME, URLDecoder.decode(userName, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				log.error("error decode userName:" + userName + " caused by:" + e.getMessage(), e);
			}
		}

		String clientIp = headers.getFirst(SystemContext.CONTEXT_CLIENT_IP);
		if (StringUtils.isNotBlank(clientIp)) {
			contextMap.put(SystemContext.CONTEXT_CLIENT_IP, clientIp);
		}

		String userAgent = headers.getFirst(SystemContext.CONTEXT_USER_AGENT);
		if (StringUtils.isNotBlank(userAgent)) {
			contextMap.put(SystemContext.CONTEXT_USER_AGENT, userAgent);
		}

		return chain.filter(exchange).subscriberContext(context -> {
			return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, contextMap);
		});
	}
}
