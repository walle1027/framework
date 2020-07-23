package org.loed.framework.common.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/11/28 10:29
 */

public class CglibProxy implements MethodInterceptor {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private Enhancer enhancer = new Enhancer();

	public <T> T getProxy(Class<T> clazz) {
		//设置需要创建子类的类
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(this);
		//通过字节码技术动态创建子类实例
		return (T) enhancer.create();
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		logger.info("i am proxy method:" + method.getName());
		Object result = methodProxy.invokeSuper(obj, args);
		return result;
	}
}
