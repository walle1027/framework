package org.loed.framework.r2dbc.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Thomason
 * @version 1.0
 * @since 2020/7/12 20:45
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Count {
	String value();
}
