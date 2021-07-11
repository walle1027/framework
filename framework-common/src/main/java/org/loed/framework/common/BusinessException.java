package org.loed.framework.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service层的公用Exception
 * <p/>
 * 继承自RuntimeException， 在服务层抛出会触发Spring的事务管理器引起事务回滚
 *
 * @author Thomason
 * @version 1.2
 * @since 2009-07-04
 */
public class BusinessException extends RuntimeException {
	private List<ErrorInfo> errors;

	public BusinessException() {
		super();
	}

	public BusinessException(String errorCode) {
		super(errorCode);
		addError(errorCode);
	}

	public BusinessException(ErrorInfo error) {
		super(error == null ? null : error.getCode());
		addError(error);
	}

	public BusinessException(List<ErrorInfo> errors) {
		if (errors != null) {
			for (ErrorInfo error : errors) {
				addError(error);
			}
		}
	}

	public BusinessException(Throwable cause) {
		super(cause);
	}

	public BusinessException(String msg, Throwable cause) {
		super(msg, cause);
	}

	private void addError(String msg) {
		ErrorInfo message = new ErrorInfo(msg, null);
		addError(message);
	}

	private void addError(ErrorInfo error) {
		if (error == null) {
			return;
		}
		if (this.errors == null) {
			errors = new ArrayList<>();
		}
		errors.add(error);
	}

	public List<ErrorInfo> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorInfo> errors) {
		this.errors = errors;
	}

	public String getErrorMsg() {
		if (errors != null) {
			return errors.stream().map(ErrorInfo::getText).collect(Collectors.joining(""));
		}
		return null;
	}
}
