package org.loed.framework.common.validator.constraintvalidators;

import org.loed.framework.common.validator.constraints.PhoneNumber;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author thomason
 * @version 1.0
 * @since 2021/12/15 1:29 PM
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
	private String region;

	@Override
	public void initialize(PhoneNumber constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
		this.region = constraintAnnotation.region();
	}

	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		switch (region) {
			case "+86":
				return s != null && s.matches("[0-9]+")
						&& (s.length() > 8) && (s.length() < 14);
			default:
				return false;
		}
	}
}
