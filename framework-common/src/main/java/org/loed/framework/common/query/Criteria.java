package org.loed.framework.common.query;

import lombok.Data;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.lambda.SerializedLambda;
import org.loed.framework.common.util.ReflectionUtils;

import javax.persistence.criteria.JoinType;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/7 下午9:31
 */
@Data
public class Criteria<T> implements Serializable {
	private Selector selector;

	private List<Condition> conditions;

	private List<SortProperty> sortProperties;

	public static <S> Criteria<S> from(Class<S> clazz) {
		return new Criteria<S>();
	}

	private static <S> Criteria<S> from(Criteria<S> criteria) {
		Criteria<S> criteriaNew = new Criteria<>();
		criteriaNew.conditions = criteria.conditions;
		criteriaNew.selector = criteria.selector;
		criteriaNew.sortProperties = criteria.sortProperties;
		return criteriaNew;
	}

	public void criterion(Condition condition) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}
		if (condition != null) {
			conditions.add(condition);
		}
	}


//	public Criteria<T> clear() {
//		this.conditions = new ArrayList<>();
//		this.sortProperties = null;
//		return this;
//	}

	public Criteria<T> selector(Selector selector) {
		this.selector = selector;
		return this;
	}

	public void sort(SortProperty sortProperty) {
		if (sortProperty == null) {
			return;
		}
		if (this.sortProperties == null) {
			this.sortProperties = new ArrayList<>();
		}
		sortProperties.add(sortProperty);
	}

//	public Criteria<T> and(String prop, Operator operator, Object value) {
//		return and(new String[]{prop}, operator, value);
//	}
//
//	public Criteria<T> and(String[] props, Operator operator, Object value) {
//		Criteria<T> criteria = Criteria.from(this);
//		if (props != null && props.length > 0) {
//			for (String prop : props) {
//				Condition condition = new Condition(prop, operator, value);
//				criteria.addCriterion(condition);
//			}
//		}
//		return criteria;
//	}
//
//	public Criteria<T> or(String prop, Operator operator, String value) {
//		return or(new String[]{prop}, operator, value);
//	}
//
//	public Criteria<T> or(String[] props, Operator operator, String value) {
//		Criteria<T> criteria = Criteria.from(this);
//		if (props != null && props.length > 1) {
//			Condition condition = new Condition();
//			List<Condition> subConditions = new ArrayList<>();
//			for (String prop : props) {
//				Condition subCondition = new Condition(prop, operator, value);
//				subCondition.setJoint(Joint.or);
//				subConditions.add(subCondition);
//			}
//			condition.setSubConditions(subConditions);
//			criteria.addCriterion(condition);
//		} else if (props != null && props.length == 1) {
//			Condition condition = new Condition(props[0], operator, value);
//			condition.setJoint(Joint.or);
//			criteria.addCriterion(condition);
//		}
//		return criteria;
//	}

	public ConditionSpec<T> and(SFunction<T, ?> lambda) {
		SerializedLambda resolve = LambdaUtils.resolve(lambda);
		String prop = ReflectionUtils.methodToProperty(resolve.getImplMethodName());
		return new ConditionSpec<>(this, Joint.and, prop);
	}

	public <S> JoinBuilder<T, S> left(SFunction<T, S> lambda) {
		String prop = LambdaUtils.getPropFromLambda(lambda);
		JoinBuilder<T, S> builder = new JoinBuilder<>(this, JoinType.LEFT, prop);
		builder.chains = new ArrayList<>();
		builder.chains.add(builder);
		return builder;
	}

	public <S> JoinBuilder<T, S> inner(SFunction<T, S> lambda) {
		String prop = LambdaUtils.getPropFromLambda(lambda);
		JoinBuilder<T, S> builder = new JoinBuilder<>(this, JoinType.INNER, prop);
		builder.chains = new ArrayList<>();
		builder.chains.add(builder);
		return builder;
	}

	public ConditionSpec<T> or(SFunction<T, ?> lambda) {
		SerializedLambda resolve = LambdaUtils.resolve(lambda);
		String prop = ReflectionUtils.methodToProperty(resolve.getImplMethodName());
		return new ConditionSpec<>(this, Joint.or, prop);
	}

	public Criteria<T> asc(SFunction<T, ?> lambda) {
		SerializedLambda resolve = LambdaUtils.resolve(lambda);
		String prop = ReflectionUtils.methodToProperty(resolve.getImplMethodName());
		this.sort(new SortProperty(prop, Sort.ASC));
		return this;
	}

	public Criteria<T> desc(SFunction<T, ?> lambda) {
		SerializedLambda resolve = LambdaUtils.resolve(lambda);
		String prop = ReflectionUtils.methodToProperty(resolve.getImplMethodName());
		this.sort(new SortProperty(prop, Sort.DESC));
		return this;
	}

	public static class JoinBuilder<T, R> {
		private final String prop;
		private final JoinType joinType;
		private List<JoinBuilder<T, ?>> chains;
		private final Criteria<T> criteria;

		private JoinBuilder(Criteria<T> criteria, JoinType joinType, String prop) {
			this.criteria = criteria;
			this.joinType = joinType;
			this.prop = prop;
		}

		public <S> JoinBuilder<T, S> left(SFunction<R, S> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			JoinBuilder<T, S> builder = new JoinBuilder<>(this.criteria, JoinType.LEFT, prop);
			builder.chains = this.chains;
			builder.chains.add(builder);
			return builder;
		}

		public <S> JoinBuilder<T, S> inner(SFunction<R, S> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			JoinBuilder<T, S> builder = new JoinBuilder<>(this.criteria, JoinType.INNER, prop);
			builder.chains = this.chains;
			builder.chains.add(builder);
			return builder;
		}

		public ConditionSpec<T> and(SFunction<R, ?> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			StringBuilder builder = new StringBuilder();
			for (JoinBuilder<T, ?> chain : this.chains) {
				builder.append(chain.prop);
				Condition condition = new Condition();
				condition.setPropertyName(builder.toString());
				condition.setJoinType(chain.joinType);
				builder.append(".");
				criteria.criterion(condition);
			}
			builder.append(prop);
			//todo remove chains
			this.chains = null;
			return new ConditionSpec<T>(criteria, Joint.and, builder.toString());
		}

		public ConditionSpec<T> or(SFunction<R, ?> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			StringBuilder builder = new StringBuilder();
			for (JoinBuilder<T, ?> chain : this.chains) {
				builder.append(chain.prop);
				Condition condition = new Condition();
				condition.setPropertyName(builder.toString());
				condition.setJoinType(chain.joinType);
				builder.append(".");
			}
			builder.append(prop);
			this.chains = null;
			return new ConditionSpec<T>(criteria, Joint.or, builder.toString());
		}
	}

	public static class ConditionSpec<T> {
		private final String property;
		private final Criteria<T> criteria;
		private final Joint joint;

		public ConditionSpec(Criteria<T> criteria, Joint joint, String property) {
			this.property = property;
			this.criteria = criteria;
			this.joint = joint;
		}

		public Criteria<T> beginWith(String value) {
			Condition condition = new Condition(property, Operator.beginWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notBeginWith(String value) {
			Condition condition = new Condition(property, Operator.notBeginWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> contains(String value) {
			Condition condition = new Condition(property, Operator.contains, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notContains(String value) {
			Condition condition = new Condition(property, Operator.notContains, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> endWith(String value) {
			Condition condition = new Condition(property, Operator.endWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notEndWith(String value) {
			Condition condition = new Condition(property, Operator.notEndWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> between(Object start, Object end) {
			Condition condition = new Condition(property, Operator.between, new Object[]{start, end});
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notBetween(Object start, Object end) {
			Condition condition = new Condition(property, Operator.notBetween, new Object[]{start, end});
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> blank() {
			Condition condition = new Condition(property, Operator.blank, null);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notBlank() {
			Condition condition = new Condition(property, Operator.notBlank, null);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> is(Object value) {
			Condition condition = new Condition(property, Operator.equal, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> isNot(Object value) {
			Condition condition = new Condition(property, Operator.notEqual, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> greaterThan(Object value) {
			Condition condition = new Condition(property, Operator.greaterThan, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> greaterEqual(Object value) {
			Condition condition = new Condition(property, Operator.greaterEqual, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> lessEqual(Object value) {
			Condition condition = new Condition(property, Operator.lessEqual, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> lessThan(Object value) {
			Condition condition = new Condition(property, Operator.lessThan, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> isNull() {
			Condition condition = new Condition(property, Operator.isNull, null);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> isNotNull() {
			Condition condition = new Condition(property, Operator.isNotNull, null);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(@NotNull Collection<?> values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(@NotNull Collection<?> values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> custom(@NotNull String value) {
			Condition condition = new Condition(property, Operator.custom, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}
	}
}
