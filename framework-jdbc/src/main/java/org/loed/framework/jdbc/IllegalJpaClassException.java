package org.loed.framework.jdbc;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/11/11 上午10:10
 */
public class IllegalJpaClassException extends RuntimeException {
	public IllegalJpaClassException() {
	}

	public IllegalJpaClassException(String message) {
		super(message);
	}

	public IllegalJpaClassException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalJpaClassException(Throwable cause) {
		super(cause);
	}

	public IllegalJpaClassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
