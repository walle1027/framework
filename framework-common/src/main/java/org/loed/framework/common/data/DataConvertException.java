package org.loed.framework.common.data;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/9 12:03 下午
 */
public class DataConvertException  extends RuntimeException {
	public DataConvertException() {
	}

	public DataConvertException(String message) {
		super(message);
	}

	public DataConvertException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataConvertException(Throwable cause) {
		super(cause);
	}

	public DataConvertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
