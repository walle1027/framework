package org.loed.framework.common.validator.constraints;

import org.loed.framework.common.validator.constraintvalidators.PhoneNumberValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2021/12/15 2:05 PM
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectCode {
	String message() default "code is only allow 0-9 a-z and - _,and must start with character";

	int length() default 20;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
