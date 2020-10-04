package org.loed.framework.common.po;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/12 7:54 上午
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LastModifyTime {
	Class<?>[] supportedTypes() default {Date.class, java.sql.Date.class, LocalDateTime.class};
}
