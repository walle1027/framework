package org.loed.framework.rpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 微服务 注解
 *
 * @author TIM(JT)
 * @date 2017-10-18 17
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ServiceProxy {
	/**
	 * 微服务名称
	 *
	 * @return 服务名称
	 */
	String baseUri() default "";

	/**
	 * webClientBeanName
	 *
	 * @return webClientBeanName
	 */
	String webClientBeanName() default "";
}
