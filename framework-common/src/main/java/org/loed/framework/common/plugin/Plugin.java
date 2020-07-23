package org.loed.framework.common.plugin;

/**
 * 插件描述的抽象父类
 *
 * @author Thomason
 * @version 1.0
 * @since 2016/11/26 13:55
 */

public abstract class Plugin {
	/**
	 * 公司编号
	 */
	private String tenantCode;
	/**
	 * 代理方法元信息
	 */
	private String signature;
	/**
	 * 代理协议
	 */
	private String protocol;

	/**
	 * 插件代理协议 目前有 http soa internal 三种机制
	 *
	 * @return 协议名称
	 */
	public abstract String getProtocol();

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getTenantCode() {
		return tenantCode;
	}

	public void setTenantCode(String tenantCode) {
		this.tenantCode = tenantCode;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Override
	public String toString() {
		return "Plugin{" +
				"tenantCode='" + tenantCode + '\'' +
				", methodSignature='" + signature + '\'' +
				", protocol='" + getProtocol() + '\'' +
				'}';
	}
}
