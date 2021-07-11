package org.loed.framework.common;

import java.io.Serializable;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/5/23 17:19
 */
public class Result<T> implements Serializable {
	public static final Result<Void> UNKNOWN_ERROR = new Result<>(503, "Unknown Server Error");

	private int code;
	private String message;
	private T data;

	public Result() {
	}

	public Result(T data) {
		this.data = data;
	}

	public Result(int code) {
		this.code = code;
	}

	public Result(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public Result(int code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
