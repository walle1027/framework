package org.loed.framework.common.po;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigInteger;


/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/11 8:23 下午
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CreateBy {
	Class<?>[] supportedTypes() default {Long.class, long.class, BigInteger.class, String.class};
}
