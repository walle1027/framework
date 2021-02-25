package org.loed.framework.common.context;

import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/5 11:58 上午
 */
public class ReactiveSystemContext {
	public static final String REACTIVE_SYSTEM_CONTEXT = "ReactiveSystemContext";

	public static Mono<SystemContext> getSystemContext() throws NoSystemContextException {
		return Mono.deferContextual(ctx -> {
			SystemContext systemContext = null;
			if (ctx.hasKey(REACTIVE_SYSTEM_CONTEXT)) {
				systemContext = ctx.get(REACTIVE_SYSTEM_CONTEXT);
			}
			if (systemContext == null) {
				systemContext = new SystemContext();
			}
			return Mono.just(systemContext);
		});
	}

//	public static Mono<String> getUserToken() throws NoSystemContextException {
//		return getSystemContext().map(SystemContext::getUserToken);
//	}
//
//	public static Mono<String> getUserId() {
//		return getSystemContext().map(SystemContext::getUserId);
//	}
//
//	public static Mono<String> getAccountId() {
//		return getSystemContext().defaultIfEmpty(new SystemContext()).map(SystemContext::getAccountId);
//	}
//
//	public static Mono<String> getTenantCode() {
//		return getSystemContext().map(SystemContext::getTenantCode).defaultIfEmpty(SystemContext.DEFAULT_TENANT_CODE);
//	}
//
//	public static Mono<String> getLocale() {
//		return getSystemContext().map(SystemContext::getLocale);
//	}

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
