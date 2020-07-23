package org.loed.framework.common.web.validate.rule;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.web.validate.annotation.IdCardCheck;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-22 下午5:57
 */

public class IdCardValidator implements ConstraintValidator<IdCardCheck, String> {
	@Override
	public void initialize(IdCardCheck idCardCheck) {
		//TODO
	}

	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		if (StringUtils.isEmpty(s)) {
			return false;
		}
		if (s.length() == 15 || s.length() == 18)
			return true;
		return false;
	}
}
