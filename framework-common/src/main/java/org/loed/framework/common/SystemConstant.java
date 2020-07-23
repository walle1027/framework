package org.loed.framework.common;


import org.loed.framework.common.po.BasePO;

import java.util.function.BiFunction;

/**
 * 定义框架系统常量的类
 *
 * @author Thomason
 * @version 1.2
 * @since 2009-03-10
 */
public interface SystemConstant {

	//提示消息
	int MSG_INFO = 0;
	//警告消息
	int MSG_WARNING = 1;
	//错误消息
	int MSG_ERROR = 2;

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

	BiFunction<BasePO, BasePO, String[]> toBasePO = (po, vo) -> new String[]{};

	BiFunction<BasePO, BasePO, String[]> fromBasePO = (vo, po) -> new String[]{"id", "version", "createTime", "updateTime", "createBy", "updateBy"};
}
