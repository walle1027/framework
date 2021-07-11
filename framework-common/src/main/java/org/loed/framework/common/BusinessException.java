package org.loed.framework.common;

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
	private final int errorCode;
	private Message message;

	public BusinessException() {
		super();
		this.errorCode = SystemConstant.SERVER_ERROR;
	}

	public BusinessException(int errorCode) {
		this.errorCode = errorCode;
	}

	public BusinessException(String message) {
		super(message);
		this.errorCode = SystemConstant.SERVER_ERROR;
		this.message = new Message(message, null);
	}

	public BusinessException(int errorCode, String message) {
		this.errorCode = errorCode;
		this.message = new Message(message, null);
	}

	public BusinessException(int errorCode, Message message) {
		this.errorCode = errorCode;
		this.message = message;
	}


	public int getErrorCode() {
		return errorCode;
	}

	@Override
	public String getMessage() {
		return message == null ? null : message.getI18nKey();
	}

	public Message getMessageInfo() {
		return this.message;
	}
}
