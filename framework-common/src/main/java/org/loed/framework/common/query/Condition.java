package org.loed.framework.common.query;


import lombok.ToString;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.lambda.SFunction;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表单上的查询条件封装类
 * 封装表单的查询条件
 *
 * @author Thomason
 * Date: 11-4-22
 * Time: 下午2:16
 * @version 1.0
 */
@ToString
public class Condition implements Serializable, Copyable<Condition> {
	/**
	 * 路径分隔符
	 */
	public static final String PATH_SEPARATOR = ".";
	/**
	 * 条件连接字符串 and or
	 */
	private Joint joint = Joint.and;
	/**
	 * 实体对应的属性名称
	 */
	private String propertyName;
	/**
	 * 查询关系符号
	 */
	private Operator operator = Operator.equal;
	/**
	 * 条件的值
	 */
	private Object value;
	/**
	 * 子条件
	 */
	private List<Condition> subConditions;

	public Condition() {
	}

	@Override
	public Condition copy(){
		Condition condition = new Condition();
		condition.joint = this.joint;
		condition.propertyName = this.propertyName;
		condition.value = this.value;
		if (this.subConditions != null && this.subConditions.size() > 0){
			condition.subConditions = this.subConditions.stream().map(Condition::copy).collect(Collectors.toList());
		}
		return condition;
	}

	public Condition(String propertyName, String value) {
		this.propertyName = propertyName;
		this.value = value;
	}

	public Condition(String propertyName, Operator operator, Object value) {
		this.propertyName = propertyName;
		this.operator = operator;
		this.value = value;
	}

	public <T> Condition(SFunction<T, ?> lambda, Operator operator, Object value) {
		this.propertyName = LambdaUtils.getPropFromLambda(lambda);
		this.operator = operator;
		this.value = value;
	}

	public boolean hasSubCondition() {
		return this.subConditions != null && this.subConditions.size() > 0;
	}

	public Joint getJoint() {
		return joint;
	}

	public void setJoint(Joint joint) {
		this.joint = joint;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public List<Condition> getSubConditions() {
		return subConditions;
	}

	public void setSubConditions(List<Condition> subConditions) {
		this.subConditions = subConditions;
	}

//	public JoinType getJoinType() {
//		return joinType;
//	}
//
//	public void setJoinType(JoinType joinType) {
//		this.joinType = joinType;
//	}
}
