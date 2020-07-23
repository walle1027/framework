package org.loed.framework.common;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/13 下午10:02
 */
public interface ConfigureConstant {
	String default_ns = "org.loed";
	String dot = ".";
	//********************redis namespace**********************//
	String redis_ns = default_ns + dot + "redis";
	String event_region_name = "event";
	String soa_region_name = "soa";
	String i18n_region_name = "i18n";
	String hibernate_region_name = "hibernate";

	String redis_regions = "regions";

	//********************SOA namespace**********************//
	String soa_ns = default_ns + dot + "soa";
	String soa_service_ns = soa_ns + dot + "service";
	String soa_client_ns = soa_ns + dot + "client";
	String soa_registry_ns = soa_ns + dot + "registry";

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
