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
 * @修改原因
 * @since 2009-07-04
 */
public class BusinessException extends RuntimeException {
	private List<Message> errors;

	public BusinessException() {
		super();
	}

	public BusinessException(String msg) {
		super(msg);
		addError(msg);
	}

	public BusinessException(Message error) {
		super(error == null ? null : error.getKey());
		addError(error);
	}

	public BusinessException(List<Message> errors) {
		if (errors != null) {
			for (Message error : errors) {
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
		Message message = new Message(msg);
		message.setType(Message.MSG_ERROR);
		addError(message);
	}

	private void addError(Message error) {
		if (error == null) {
			return;
		}
		if (this.errors == null) {
			errors = new ArrayList<>();
		}
		errors.add(error);
	}

	public List<Message> getErrors() {
		return errors;
	}

	public void setErrors(List<Message> errors) {
		this.errors = errors;
	}


	public String getErrorMsg() {
		if (errors != null) {
			return String.join("", errors.stream().map(SimpleMessage::getText).collect(Collectors.toList()));
		}
		return null;
	}
}
