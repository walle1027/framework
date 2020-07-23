package org.loed.framework.jdbc.datasource.readwriteisolate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/11/10 19:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ReadWriteIsolation {
	ReadWriteStrategy value() default ReadWriteStrategy.write;
	ReadWriteIsolatePropagation propagation() default ReadWriteIsolatePropagation.required;
}
