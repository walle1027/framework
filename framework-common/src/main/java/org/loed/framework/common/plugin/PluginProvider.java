package org.loed.framework.common.plugin;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/11/26 20:09
 */

public interface PluginProvider {
	String ALL_SIGN = "__ALL__";

	/**
	 * 判断当前公司是否是否有插件
	 *
	 * @param tenantCode 公司编号
	 * @param signature  方法元信息
	 * @return true 有插件 false 无插件
	 */
	boolean hasPlugin(String tenantCode, String signature);

	/**
	 * 获取公司插件信息
	 *
	 * @param tenantCode 公司编号
	 * @param signature  方法元信息
	 * @return 插件信息
	 */
	Plugin getPlugin(String tenantCode, String signature);
}
