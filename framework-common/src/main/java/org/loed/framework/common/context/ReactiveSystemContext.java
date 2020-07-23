package org.loed.framework.common.context;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/5 11:58 上午
 */
public class ReactiveSystemContext {
	public static final String REACTIVE_SYSTEM_CONTEXT = "ReactiveSystemContext";

	public static Mono<Map<String, String>> getContextMap() throws NoSystemContextException {
		return Mono.subscriberContext().handle((ctx, sink) -> {
			if (ctx.hasKey(REACTIVE_SYSTEM_CONTEXT)) {
				sink.next(ctx.get(REACTIVE_SYSTEM_CONTEXT));
			} else {
				Map<String, String> contextMap = new HashMap<>();
				ctx.put(REACTIVE_SYSTEM_CONTEXT, contextMap);
				sink.next(contextMap);
			}
		});
	}

	public static Mono<String> getUserToken() throws NoSystemContextException {
		return getContextMap().map(map -> {
			return map.get(SystemContext.CONTEXT_USER_TOKEN);
		});
	}

	public static Mono<String> getUserId() {
		return getContextMap().map(map -> {
			return map.get(SystemContext.CONTEXT_USER_ID);
		});
	}

	public static Mono<String> getAccountId() {
		return getContextMap().map(map -> {
			return map.get(SystemContext.CONTEXT_ACCOUNT_ID);
		});
	}

	public static Mono<String> getTenantCode() {
		return getContextMap().map(map -> {
			return map.get(SystemContext.CONTEXT_TENANT_CODE);
		});
	}

	public static class NoSystemContextException extends RuntimeException {
		public NoSystemContextException() {
		}

		public NoSystemContextException(String message) {
			super(message);
		}

		public NoSystemContextException(String message, Throwable cause) {
			super(message, cause);
		}

		public NoSystemContextException(Throwable cause) {
			super(cause);
		}

		public NoSystemContextException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}
}
