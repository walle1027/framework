package org.loed.framework.mybatis.interceptor;

public class DuplicateOrderException extends RuntimeException {

	public DuplicateOrderException(String message) {
		super(message);
	}
}
