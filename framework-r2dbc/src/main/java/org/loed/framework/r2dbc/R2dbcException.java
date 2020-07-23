package org.loed.framework.r2dbc;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/4/19 9:46 AM
 */
public class R2dbcException extends RuntimeException {
	public R2dbcException() {
	}

	public R2dbcException(String message) {
		super(message);
	}

	public R2dbcException(String message, Throwable cause) {
		super(message, cause);
	}

	public R2dbcException(Throwable cause) {
		super(cause);
	}

	public R2dbcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
