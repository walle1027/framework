package org.loed.framework.common.validator.constraints;

import org.loed.framework.common.validator.constraintvalidators.PhoneNumberValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2021/12/15 1:28 PM
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {
	String message() default "Invalid phone number";

	String region() default "+86";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
