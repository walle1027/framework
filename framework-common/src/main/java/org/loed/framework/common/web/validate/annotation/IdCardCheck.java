package org.loed.framework.common.web.validate.annotation;


import org.loed.framework.common.web.validate.rule.IdCardValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * 身份证校验的标记类
 *
 * @author Thomason
 * @version 1.0
 * @since 11-11-21 下午10:10
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IdCardValidator.class)
@Documented
public @interface IdCardCheck {
	String message() default "{}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
