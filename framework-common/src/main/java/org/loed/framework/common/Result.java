package org.loed.framework.common;

import java.io.Serializable;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/5/23 17:19
 */
public class Result<T> implements Serializable {
	public static final Result<Void> UNKNOWN_ERROR = new Result<>(503, "Unknown Server Error");

	private int status;
	private String message;
	private T data;

	public Result() {
	}

	public Result(T data) {
		this.data = data;
	}

	public Result(int status) {
		this.status = status;
	}

	public Result(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public Result(int status, String message, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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
