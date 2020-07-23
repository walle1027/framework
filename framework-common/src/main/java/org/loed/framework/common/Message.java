package org.loed.framework.common;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 封装系统消息的类
 *
 * @author Thomason
 * @version 1.0
 */
public class Message extends SimpleMessage implements Serializable {
	//提示消息
	public static int MSG_INFO = 0;
	//警告消息
	public static int MSG_WARNING = 1;
	//错误消息
	public static int MSG_ERROR = 2;
	/**
	 * 消息的key
	 */
	private String key;
	/**
	 * 消息参数
	 */
	private Object[] args;
	/**
	 * 消息是否格式化过
	 */
	private boolean formatted = false;

	/**
	 * 默认的构造函数
	 */
	public Message() {
	}

	/**
	 * 带参数的构造函数
	 *
	 * @param key 消息i18nKey
	 */
	public Message(String key) {
		this.key = key;
	}

	/**
	 * 带参数的构造函数
	 *
	 * @param key  消息i18nKey
	 * @param type 消息类型
	 */
	public Message(String key, int type) {
		this.key = key;
		setType(type);
	}

	/**
	 * 默认的构造函数
	 */
	public Message(String key, Object[] args) {
		this.key = key;
		this.args = args;
	}

	/**
	 * 带参数的构造函数
	 *
	 * @param type 消息类型
	 * @param key  消息i18nKey
	 */
	public Message(int type, String key, Object[] args) {
		setType(type);
		this.key = key;
		this.args = args;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public boolean isFormatted() {
		return formatted;
	}

	public void setFormatted(boolean formatted) {
		this.formatted = formatted;
	}

	@Override
	public String toString() {
		String typeStr = "";
		if (getType() == 0) {
			typeStr = "提示";
		} else if (getType() == 1) {
			typeStr = "警告";
		} else if (getType() == 2) {
			typeStr = "错误";
		}
		return "Message{" +
				"type='" + typeStr + '\'' +
				",key='" + key + '\'' +
				", args=" + Arrays.toString(args) +
				", text=" + getText() +
				'}';
	}
}
