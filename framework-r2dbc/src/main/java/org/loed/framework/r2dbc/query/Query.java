package org.loed.framework.r2dbc.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/7 3:39 下午
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
	String value();
}
