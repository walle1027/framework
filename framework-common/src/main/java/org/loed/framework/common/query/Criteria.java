package org.loed.framework.common.query;

import lombok.Data;
import lombok.ToString;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.lambda.SerializedLambda;
import org.loed.framework.common.util.ReflectionUtils;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.JoinType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/7 下午9:31
 */
@Data
@ToString
public class Criteria<T> implements Serializable {
	private PropertySelector selector;

	private List<Condition> conditions;

	private List<SortProperty> sortProperties;

	public static <S> Criteria<S> from(Class<S> clazz) {
		return new Criteria<S>();
	}

	public static <S> Criteria<S> from(Criteria<S> criteria) {
		Criteria<S> criteriaNew = new Criteria<>();
		criteriaNew.conditions = criteria.conditions;
		criteriaNew.selector = criteria.selector;
		criteriaNew.sortProperties = criteria.sortProperties;
		return criteriaNew;
	}

	public Criteria<T> criterion(Condition... condition) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}
		if (condition != null && condition.length > 0) {
			conditions.addAll(Arrays.asList(condition));
		}
		return this;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public Criteria<T> selector(PropertySelector selector) {
		this.selector = selector;
		return this;
	}

	@SafeVarargs
	public final Criteria<T> includes(SFunction<T, ?>... functions) {
		if (functions == null || functions.length == 0) {
			return this;
		}
		if (this.selector == null) {
			this.selector = new PropertySelector();
		}
		for (SFunction<T, ?> function : functions) {
			String prop = LambdaUtils.getPropFromLambda(function);
			this.selector.include(prop);
		}
		return this;
	}

	@SafeVarargs
	public final Criteria<T> excludes(SFunction<T, ?>... functions) {
		if (functions == null || functions.length == 0) {
			return this;
		}
		if (this.selector == null) {
			this.selector = new PropertySelector();
		}
		for (SFunction<T, ?> function : functions) {
			String prop = LambdaUtils.getPropFromLambda(function);
			this.selector.exclude(prop);
		}
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

	public <S> JoinBuilder<T, S> right(SFunction<T, S> lambda) {
		String prop = LambdaUtils.getPropFromLambda(lambda);
		JoinBuilder<T, S> builder = new JoinBuilder<>(this, JoinType.RIGHT, prop);
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
			//remove chains
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

		public Criteria<T> asc(SFunction<T, ?> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			StringBuilder builder = new StringBuilder();
			for (JoinBuilder<T, ?> chain : this.chains) {
				builder.append(chain.prop);
				builder.append(".");
			}
			builder.append(prop);
			this.chains = null;
			this.criteria.sort(new SortProperty(builder.toString(), Sort.ASC));
			return this.criteria;
		}

		public Criteria<T> desc(SFunction<T, ?> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			StringBuilder builder = new StringBuilder();
			for (JoinBuilder<T, ?> chain : this.chains) {
				builder.append(chain.prop);
				builder.append(".");
			}
			builder.append(prop);
			this.chains = null;
			this.criteria.sort(new SortProperty(builder.toString(), Sort.DESC));
			return this.criteria;
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

		public Criteria<T> beginWith(@NonNull String value) {
			Condition condition = new Condition(property, Operator.beginWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notBeginWith(@NonNull String value) {
			Condition condition = new Condition(property, Operator.notBeginWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> contains(@NonNull String value) {
			Condition condition = new Condition(property, Operator.contains, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notContains(@NonNull String value) {
			Condition condition = new Condition(property, Operator.notContains, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> endWith(@NonNull String value) {
			Condition condition = new Condition(property, Operator.endWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notEndWith(@NonNull String value) {
			Condition condition = new Condition(property, Operator.notEndWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> between(@NonNull Object start, @NonNull Object end) {
			Condition condition = new Condition(property, Operator.between, new Object[]{start, end});
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notBetween(@NonNull Object start, @NonNull Object end) {
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

		public Criteria<T> is(@NonNull Object value) {
			Condition condition = new Condition(property, Operator.equal, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> isNot(@NonNull Object value) {
			Condition condition = new Condition(property, Operator.notEqual, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> greaterThan(@NonNull Object value) {
			Condition condition = new Condition(property, Operator.greaterThan, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> greaterEqual(@NonNull Object value) {
			Condition condition = new Condition(property, Operator.greaterEqual, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> lessEqual(@NonNull Object value) {
			Condition condition = new Condition(property, Operator.lessEqual, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> lessThan(@NonNull Object value) {
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

		public Criteria<T> in(@NonNull Collection<?> values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(int[] values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(long[] values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(char[] values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(double[] values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(byte[] values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(short[] values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(boolean[] values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(float[] values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(Object[] values) {
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(@NonNull Collection<?> values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(int[] values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(long[] values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(char[] values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(double[] values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(byte[] values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(short[] values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(boolean[] values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(float[] values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(Object[] values) {
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> custom(@NonNull String value) {
			Condition condition = new Condition(property, Operator.custom, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}
	}
}
