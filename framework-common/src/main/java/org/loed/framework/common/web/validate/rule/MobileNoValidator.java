package org.loed.framework.common.web.validate.rule;


import org.loed.framework.common.web.validate.annotation.MobileNoCheck;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-22 下午5:55
 */

public class MobileNoValidator implements ConstraintValidator<MobileNoCheck, String> {
	@Override
	public void initialize(MobileNoCheck mobileNoCheck) {
		//TODO

	}

	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		//TODO
		return false;
	}
}
