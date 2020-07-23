package org.loed.framework.common;

import java.io.Serializable;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-5-29 上午12:04
 */
public class SimpleMessage implements Serializable {
	/**
	 * 消息文本
	 */
	private String text;
	/**
	 * 消息类型
	 */
	private int type;

	/**
	 * 默认的构造函数
	 */
	public SimpleMessage() {
	}

	/**
	 * 带参数的构造函数
	 *
	 * @param text 消息文本
	 */
	public SimpleMessage(String text) {
		this.text = text;
	}

	/**
	 * 带参数的构造函数
	 *
	 * @param text 消息文本
	 * @param type 消息类型
	 */
	public SimpleMessage(String text, int type) {
		this.text = text;
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
