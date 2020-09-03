package org.loed.framework.common.query;

/**
 * 关系操作符号
 *
 * @author Thomason
 * @version 1.0
 */
public enum Operator {
	/**
	 * sql operators
	 */
	beginWith("like"),
	notBeginWith("not like"),
	contains("like"),
	notContains("not like"),
	endWith("like"),
	notEndWith("not like"),
	between("between"),
	notBetween("not between"),
	blank(" = ''"),
	notBlank(" != ''"),
	equal("="),
	notEqual("!="),
	greaterThan(">"),
	greaterEqual(">="),
	lessEqual("<="),
	lessThan("<"),
	isNull("is null"),
	isNotNull("is not null"),
	in("in"),
	notIn("not in"),
	custom("");

	/**
	 * operator values
	 */
	private final String value;

	Operator(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}
