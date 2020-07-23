package org.loed.framework.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/1/22 11:24 AM
 */
public class SpringUtils {
	public static ApplicationContext applicationContext;

	public static void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringUtils.applicationContext = applicationContext;
	}

	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}

	public static <T> T getBean(String beanName) {
		return (T) applicationContext.getBean(beanName);
	}
}
