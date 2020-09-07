package org.loed.framework.common.query;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.lambda.CFunction;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.lambda.SerializedLambda;
import org.loed.framework.common.util.ReflectionUtils;
import org.springframework.lang.NonNull;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.criteria.JoinType;
import java.io.Serializable;
import java.util.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/7 下午9:31
 */
@Data
@ToString
@Slf4j
@NotThreadSafe
public final class Criteria<T> implements Serializable {
	private PropertySelector selector;

	private List<Condition> conditions;

	private List<SortProperty> sortProperties;

	private TreeMap<String, Join> joins;

	public static <S> Criteria<S> from(Class<S> clazz) {
		return new Criteria<S>();
	}

	public static <S> Criteria<S> from(Criteria<S> criteria) {
		Criteria<S> criteriaNew = new Criteria<>();
		criteriaNew.conditions = criteria.conditions;
		criteriaNew.selector = criteria.selector;
		criteriaNew.sortProperties = criteria.sortProperties;
		criteriaNew.joins = criteria.joins;
		return criteriaNew;
	}

	private Criteria() {
		this.conditions = new ArrayList<>();
	}

	private void criterion(Condition... condition) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}
		if (condition != null && condition.length > 0) {
			conditions.addAll(Arrays.asList(condition));
		}
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
	public final Criteria<T> includes(CascadeProperty<T, ?>... cascadeProperties) {
		if (cascadeProperties == null || cascadeProperties.length == 0) {
			return this;
		}
		if (this.selector == null) {
			this.selector = new PropertySelector();
		}
		for (CascadeProperty<T, ?> function : cascadeProperties) {
			String joinProperty = function.joinProperty;
			String cascadeProperty = function.cascadeProperty;
			if (joinProperty != null && (this.joins == null || !this.joins.containsKey(joinProperty))) {
				throw new RuntimeException("the cascade property :" + cascadeProperty + " has'nt joins,please add joins first");
			}
			this.selector.include(cascadeProperty);
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

	@SafeVarargs
	public final Criteria<T> excludes(CascadeProperty<T, ?>... cascadeProperties) {
		if (cascadeProperties == null || cascadeProperties.length == 0) {
			return this;
		}
		if (this.selector == null) {
			this.selector = new PropertySelector();
		}
		for (CascadeProperty<T, ?> property : cascadeProperties) {
			String joinProperty = property.joinProperty;
			String cascadeProperty = property.cascadeProperty;
			if (joinProperty != null && (this.joins == null || !this.joins.containsKey(joinProperty))) {
				throw new RuntimeException("the cascade property :" + cascadeProperty + " has'nt joins,please add joins first");
			}
			this.selector.exclude(property.cascadeProperty);
		}
		return this;
	}

	private void sort(SortProperty sortProperty) {
		if (sortProperty == null) {
			return;
		}
		if (this.sortProperties == null) {
			this.sortProperties = new ArrayList<>();
		}
		sortProperties.add(sortProperty);
	}

	public <S> JoinBuilder<T, S> leftJoin(SFunction<T, S> lambda) {
		return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.LEFT);
	}

	public <S> JoinBuilder<T, S> leftJoin(CFunction<T, S> lambda) {
		return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.LEFT);
	}

	public <S> JoinBuilder<T, S> innerJoin(SFunction<T, S> lambda) {
		return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.INNER);
	}

	public <S> JoinBuilder<T, S> innerJoin(CFunction<T, S> lambda) {
		return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.INNER);
	}

	public <S> JoinBuilder<T, S> rightJoin(SFunction<T, S> lambda) {
		return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.RIGHT);
	}

	public <S> JoinBuilder<T, S> rightJoin(CFunction<T, S> lambda) {
		return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.RIGHT);
	}

	private <S> JoinBuilder<T, S> joinBuilder(String prop, JoinType left) {
		Join join = new Join();
		join.setTarget(prop);
		join.setUniquePath(prop);
		join.setJoinType(left);
		join(join);
		return new JoinBuilder<>(this, join, null);
	}

	public ConditionSpec<T> and(SFunction<T, ?> lambda) {
		SerializedLambda resolve = LambdaUtils.resolve(lambda);
		String prop = ReflectionUtils.methodToProperty(resolve.getImplMethodName());
		return new ConditionSpec<>(this, Joint.and, prop);
	}

	public ConditionSpec<T> or(SFunction<T, ?> lambda) {
		SerializedLambda resolve = LambdaUtils.resolve(lambda);
		String prop = ReflectionUtils.methodToProperty(resolve.getImplMethodName());
		return new ConditionSpec<>(this, Joint.or, prop);
	}

	@SafeVarargs
	public final Criteria<T> asc(SFunction<T, ?>... lambda) {
		if (lambda == null || lambda.length == 0) {
			return this;
		}
		for (SFunction<T, ?> function : lambda) {
			String prop = LambdaUtils.getPropFromLambda(function);
			this.sort(new SortProperty(prop, Sort.ASC));
		}
		return this;
	}

	@SafeVarargs
	public final Criteria<T> asc(CascadeProperty<T, ?>... cascadeProperties) {
		if (cascadeProperties == null || cascadeProperties.length == 0) {
			return this;
		}
		for (CascadeProperty<T, ?> property : cascadeProperties) {
			String joinProperty = property.joinProperty;
			String cascadeProperty = property.cascadeProperty;
			if (joinProperty != null && (this.joins == null || !this.joins.containsKey(joinProperty))) {
				throw new RuntimeException("the cascade property :" + cascadeProperty + " has'nt joins,please add joins first");
			}
			this.sort(new SortProperty(property.cascadeProperty, Sort.ASC));
		}
		return this;
	}

	@SafeVarargs
	public final Criteria<T> desc(SFunction<T, ?>... lambda) {
		if (lambda == null || lambda.length == 0) {
			return this;
		}
		for (SFunction<T, ?> function : lambda) {
			String prop = LambdaUtils.getPropFromLambda(function);
			this.sort(new SortProperty(prop, Sort.DESC));
		}
		return this;
	}

	@SafeVarargs
	public final Criteria<T> desc(CascadeProperty<T, ?>... cascadeProperties) {
		if (cascadeProperties == null || cascadeProperties.length == 0) {
			return this;
		}
		for (CascadeProperty<T, ?> property : cascadeProperties) {
			String joinProperty = property.joinProperty;
			String cascadeProperty = property.cascadeProperty;
			if (joinProperty != null && (this.joins == null || !this.joins.containsKey(joinProperty))) {
				throw new RuntimeException("the cascade property :" + cascadeProperty + " has'nt joins,please add joins first");
			}
			this.sort(new SortProperty(cascadeProperty, Sort.DESC));
		}
		return this;
	}

	private void join(Join join) {
		if (this.joins == null) {
			this.joins = new TreeMap<>();
		}
		if (this.joins.containsKey(join.getUniquePath())) {
			Join exists = this.joins.get(join.getUniquePath());
			if (Objects.equals(exists, join)) {
				log.error("same path with different joins ");
				throw new RuntimeException("same path with different joins " + join);
			}
		} else {
			this.joins.put(join.getUniquePath(), join);
		}
	}

	public static class JoinBuilder<T, R> {
		private final Join join;
		private final JoinBuilder<T, ?> previous;
		private final Criteria<T> criteria;

		private JoinBuilder(Criteria<T> criteria, Join join, JoinBuilder<T, ?> previous) {
			this.criteria = criteria;
			this.join = join;
			this.previous = previous;
		}

		public <S> JoinBuilder<T, S> leftJoin(SFunction<R, S> lambda) {
			return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.LEFT);
		}

		public <S> JoinBuilder<T, S> leftJoin(CFunction<R, S> lambda) {
			return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.LEFT);
		}

		public <S> JoinBuilder<T, S> innerJoin(SFunction<R, S> lambda) {
			return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.INNER);
		}

		public <S> JoinBuilder<T, S> innerJoin(CFunction<R, S> lambda) {
			return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.INNER);
		}

		public <S> JoinBuilder<T, S> rightJoin(SFunction<R, S> lambda) {
			return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.RIGHT);
		}

		public <S> JoinBuilder<T, S> rightJoin(CFunction<R, S> lambda) {
			return joinBuilder(LambdaUtils.getPropFromLambda(lambda), JoinType.RIGHT);
		}

		private <S> JoinBuilder<T, S> joinBuilder(String prop, JoinType inner) {
			Join join = new Join();
			join.setJoinType(inner);
			join.setTarget(prop);
			join.setUniquePath(this.join.getUniquePath() + "." + prop);
			this.criteria.join(join);
			return new JoinBuilder<>(this.criteria, join, this);
		}

		public ConditionSpec<T> and(SFunction<R, ?> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			return new ConditionSpec<T>(criteria, Joint.and, this.join.getUniquePath() + Condition.PATH_SEPARATOR + prop);
		}

		public ConditionSpec<T> or(SFunction<R, ?> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			return new ConditionSpec<T>(criteria, Joint.and, this.join.getUniquePath() + Condition.PATH_SEPARATOR + prop);
		}

		@SafeVarargs
		public final Criteria<T> asc(SFunction<T, ?>... lambda) {
			if (lambda == null || lambda.length == 0) {
				return this.criteria;
			}
			for (SFunction<T, ?> function : lambda) {
				String prop = LambdaUtils.getPropFromLambda(function);
				String sortProp = this.join.getUniquePath() + Condition.PATH_SEPARATOR + prop;
				this.criteria.sort(new SortProperty(sortProp, Sort.ASC));
			}
			return this.criteria;
		}

		@SafeVarargs
		public final Criteria<T> desc(SFunction<T, ?>... lambda) {
			if (lambda == null || lambda.length == 0) {
				return this.criteria;
			}
			for (SFunction<T, ?> function : lambda) {
				String prop = LambdaUtils.getPropFromLambda(function);
				String sortProp = this.join.getUniquePath() + Condition.PATH_SEPARATOR + prop;
				this.criteria.sort(new SortProperty(sortProp, Sort.DESC));
			}
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

	public static class CascadeProperty<T, R> {
		private final String cascadeProperty;
		private final String joinProperty;

		public CascadeProperty(String cascadeProperty, String joinProperty) {
			this.cascadeProperty = cascadeProperty;
			this.joinProperty = joinProperty;
		}

		public static <T, R> CascadeProperty<T, R> from(SFunction<T, R> lambda) {
			String property = LambdaUtils.getPropFromLambda(lambda);
			return new CascadeProperty<>(property, null);
		}

		public <S> CascadeProperty<T, S> next(SFunction<R, S> lambda) {
			String property = LambdaUtils.getPropFromLambda(lambda);
			return new CascadeProperty<>(this.cascadeProperty + Condition.ALIAS_SEPARATOR + property, this.cascadeProperty);
		}

		public <S> CascadeProperty<T, S> next(CFunction<R, S> lambda) {
			String property = LambdaUtils.getPropFromLambda(lambda);
			return new CascadeProperty<>(this.cascadeProperty + Condition.ALIAS_SEPARATOR + property, this.cascadeProperty);
		}
	}
}
