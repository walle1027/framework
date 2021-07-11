package org.loed.framework.common;

import java.io.Serializable;

/**
 * 封装系统消息的类
 *
 * @author Thomason
 * @version 1.0
 */
public class ErrorInfo implements Serializable {
	/**
	 * 消息的key
	 */
	private final String code;
	/**
	 * 消息文本
	 */
	private String text;
	/**
	 * 消息参数
	 */
	private final Object[] args;
	/**
	 * 消息是否格式化过
	 */
	private boolean formatted = false;
	/**
	 * 构造函数
	 *
	 * @param code  消息的key
	 * @param args 消息的参数
	 */
	public ErrorInfo(String code, Object[] args) {
		this.code = code;
		this.args = args;
	}

	public String getCode() {
		return code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Object[] getArgs() {
		return args;
	}

	public boolean isFormatted() {
		return formatted;
	}

	public void setFormatted(boolean formatted) {
		this.formatted = formatted;
	}
}
