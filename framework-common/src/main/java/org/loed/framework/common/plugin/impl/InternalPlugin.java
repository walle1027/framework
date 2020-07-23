package org.loed.framework.common.plugin.impl;

import org.loed.framework.common.plugin.Plugin;
import org.loed.framework.common.plugin.PluginProtocol;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/11/28 10:27
 */

public class InternalPlugin extends Plugin {
	//内部对象是否是spring的bean 默认是
	private boolean isSpringBean = true;
	//内部另外一个实现的ioc容器的 beanId
	private String beanName;
	//内部实现的另一个对象类型，如果不是spring的bean将反射创建此类型对应的bean，然后调用方法
	private String beanClass;
	//内部实现的对象对应的方法名(如果为null，则默认用原方法)
	private String method;

	@Override
	public String getProtocol() {
		return PluginProtocol.internal.name();
	}

	public boolean isSpringBean() {
		return isSpringBean;
	}

	public void setSpringBean(boolean springBean) {
		isSpringBean = springBean;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(String beanClass) {
		this.beanClass = beanClass;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}


	@Override
	public String toString() {
		return "InternalPlugin{" +
				"isSpringBean=" + isSpringBean +
				", beanId='" + beanName + '\'' +
				", beanType='" + beanClass + '\'' +
				", methodName='" + method + '\'' +
				"} " + super.toString();
	}
}
