package org.loed.framework.common.po;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/12 7:57 上午
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsDeleted {
	Class<?>[] supportedTypes() default {Integer.class, int.class, Byte.class, byte.class};
}
