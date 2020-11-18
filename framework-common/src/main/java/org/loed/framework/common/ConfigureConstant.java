package org.loed.framework.common;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/13 下午10:02
 */
public interface ConfigureConstant {
	String default_ns = "org.loed.framework";
	String dot = ".";
	//********************redis namespace**********************//
	String redis_ns = default_ns + dot + "redis";
	String event_region_name = "event";

	//********************SOA namespace**********************//
	String soa_ns = default_ns + dot + "soa";
	String MYBATIS_NS = default_ns + dot + "mybatis";

	//********************datasource namespace****************//
	String datasource_ns = default_ns + dot + "datasource";

	String message_ns = default_ns + dot + "message";

	String base_package_name = "org.loed";

	String pointcut = "execution(* " + base_package_name + "..impl.*ServiceImpl.*(..))";

	//********************database namespace****************//
	String database_ns = default_ns + dot + "database";

	//**********************webconfig********************************//
	String web_config_ns = default_ns + dot + "web";

	String filter_config_ns = web_config_ns + dot + "filter";
}
