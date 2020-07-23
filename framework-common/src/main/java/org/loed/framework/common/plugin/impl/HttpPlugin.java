package org.loed.framework.common.plugin.impl;

import org.apache.http.client.methods.HttpPost;
import org.loed.framework.common.plugin.Plugin;
import org.loed.framework.common.plugin.PluginProtocol;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/11/28 10:26
 */

public class HttpPlugin extends Plugin {
	//http请求地址
	private String url;
	//http请求方法get,post
	private String method = HttpPost.METHOD_NAME;
	//http请求数据传输类型xml,json,default
	private String dataType;
	//返回结果类型
	private String response;

	@Override
	public String getProtocol() {
		return PluginProtocol.http.name();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}
