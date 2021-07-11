package org.loed.framework.common;

import java.io.Serializable;

/**
 * 封装系统消息的类
 *
 * @author Thomason
 * @version 1.0
 */
public class Message implements Serializable {
	/**
	 * 消息文本
	 */
	private final String i18nKey;
	/**
	 * 消息参数
	 */
	private final Object[] args;
	/**
	 * 消息文本
	 */
	private String text;
	/**
	 * 消息是否格式化过
	 */
	private boolean formatted = false;

	public Message(String i18nKey, Object[] args) {
		this.i18nKey = i18nKey;
		this.args = args;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public Object[] getArgs() {
		return args;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isFormatted() {
		return formatted;
	}

	public void setFormatted(boolean formatted) {
		this.formatted = formatted;
	}
}
