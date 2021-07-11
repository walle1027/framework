package org.loed.framework.common;

/**
 * 定义框架系统常量的类
 *
 * @author Thomason
 * @version 1.2
 * @since 2009-03-10
 */
public interface SystemConstant {
	//错误消息
	String SERVER_ERROR = "503";

	String RESPONSE_WRAPPED ="ResponseWrapped";

	//*******************http请求响应类型**************/
	String RESPONSE_TYPE_JSON = "json";
	String RESPONSE_TYPE_XML = "xml";
	String RESPONSE_TYPE_TEXT = "text";
	String RESPONSE_TYPE_HTML = "html";

	//****************domain KEY*************/
	String DOMAIN_KEY_DOMAIN_NAME = "domainName";
	String DOMAIN_KEY_TENANT_CODE = "tenantCode";
	String DOMAIN_KEY_COOKIE_DOAMIN = "sessionIdKey";


	String DEFAULT_VERSION = "default";

	String DEFAULT_TENANT_CODE = "loed";
}
