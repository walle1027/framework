package org.loed.framework.common.validator.constraintvalidators;

import org.loed.framework.common.validator.constraints.ObjectCode;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author thomason
 * @version 1.0
 * @since 2021/12/15 3:58 PM
 */
public class ObjectCodeValidator implements ConstraintValidator<ObjectCode, String> {
	private static final String CODE_PATTERN = "^[A-Za-z0-9_-]+$";

	private static final String SINGLE_CHAR_PATTERN = "^[a-zA-Z]$";

	private int length;

	@Override
	public void initialize(ObjectCode constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
		this.length = constraintAnnotation.length();
	}

	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		if (s == null || "".equals(s)) {
			return false;
		}
		if (s.length() > length) {
			return false;
		}
		String firstChar = s.substring(0, 1);
		if (!firstChar.matches(SINGLE_CHAR_PATTERN)) {
			return false;
		}
		return s.matches(CODE_PATTERN);
	}
}
