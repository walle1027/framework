package org.loed.framework.common.query;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/7 下午9:31
 */
@Data
@ToString
@Slf4j
@NotThreadSafe
public final class Criteria<T> implements Serializable, Copyable<Criteria<T>> {
	private PropertySelector selector;

	private List<Condition> conditions;

	private List<SortProperty> sortProperties;

	private TreeMap<String, Join> joins;

	private final boolean lenient;

	public static <S> Criteria<S> empty() {
		return new Criteria<>(false);
	}

	public static <S> Criteria<S> from(Class<S> clazz) {
		return new Criteria<>(false);
	}

	public static <S> Criteria<S> from(Class<S> clazz, boolean lenient) {
		return new Criteria<>(lenient);
	}

	private Criteria(boolean lenient) {
		this.conditions = new ArrayList<>();
		this.lenient = lenient;
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
//		Criteria<T> criteria = this.copy();
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
//		Criteria<T> criteria = this.copy();
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
//		Criteria<T> criteria = this.copy();
		Join join = new Join();
		join.setTarget(prop);
		join.setUniquePath(prop);
		join.setJoinType(left);
		this.join(join);
		return new JoinBuilder<>(this, join);
	}

	public <R> ConditionSpec<T, R> where(SFunction<T, R> lambda) {
		return and(lambda);
	}

	public <R> ConditionSpec<T, R> and(SFunction<T, R> lambda) {
		SerializedLambda resolve = LambdaUtils.resolve(lambda);
		String prop = ReflectionUtils.methodToProperty(resolve.getImplMethodName());
		return new ConditionSpec<>(this, Joint.and, prop);
	}

	public <R> ConditionSpec<T, R> or(SFunction<T, R> lambda) {
		SerializedLambda resolve = LambdaUtils.resolve(lambda);
		String prop = ReflectionUtils.methodToProperty(resolve.getImplMethodName());
		return new ConditionSpec<>(this, Joint.or, prop);
	}

	@SafeVarargs
	public final Criteria<T> asc(SFunction<T, ?>... lambda) {
		if (lambda == null || lambda.length == 0) {
			return this;
		}
//		Criteria<T> criteria = this.copy();
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
//		Criteria<T> criteria = this.copy();
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
//		Criteria<T> criteria = this.copy();
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
//		Criteria<T> criteria = this.copy();
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

	private void sort(SortProperty sortProperty) {
		if (sortProperty == null) {
			return;
		}
		if (this.sortProperties == null) {
			this.sortProperties = new ArrayList<>();
		}
		sortProperties.add(sortProperty);
	}

	private void criterion(Condition... condition) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}
		if (condition != null && condition.length > 0) {
			conditions.addAll(Arrays.asList(condition));
		}
	}

	private void join(Join join) {
		if (this.joins == null) {
			this.joins = new TreeMap<>();
		}
		if (this.joins.containsKey(join.getUniquePath())) {
			Join exists = this.joins.get(join.getUniquePath());
			if (!Objects.equals(exists, join)) {
				log.error("same path with different joins ");
				throw new RuntimeException("same path with different joins " + join);
			}
		} else {
			this.joins.put(join.getUniquePath(), join);
		}
	}

	@Override
	public Criteria<T> copy() {
		Criteria<T> criteria = new Criteria<>(this.lenient);
		criteria.conditions = this.conditions.stream().map(Condition::copy).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(this.sortProperties)) {
			criteria.sortProperties = this.sortProperties.stream().map(SortProperty::copy).collect(Collectors.toList());
			;
		}
		if (this.selector != null) {
			criteria.selector = this.selector.copy();
		}
		if (this.joins != null && !this.joins.isEmpty()) {
			TreeMap<String, Join> map = new TreeMap<>();
			for (Map.Entry<String, Join> entry : joins.entrySet()) {
				map.put(entry.getKey(), entry.getValue().copy());
			}
			criteria.joins = map;
		}
		return criteria;
	}

	public static class JoinBuilder<T, R> {
		private final Join join;
		private final Criteria<T> criteria;

		private JoinBuilder(Criteria<T> criteria, Join join) {
			this.criteria = criteria;
			this.join = join;
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
//			Criteria<T> criteria = this.criteria.copy();
			Join join = new Join();
			join.setJoinType(inner);
			join.setTarget(prop);
			join.setUniquePath(this.join.getUniquePath() + "." + prop);
			criteria.join(join);
			return new JoinBuilder<>(criteria, join);
		}

		public <S> ConditionSpec<T, S> and(SFunction<R, S> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			return new ConditionSpec<>(criteria, Joint.and, this.join.getUniquePath() + Condition.PATH_SEPARATOR + prop);
		}

		public <S> ConditionSpec<T, S> or(SFunction<R, S> lambda) {
			String prop = LambdaUtils.getPropFromLambda(lambda);
			return new ConditionSpec<>(criteria, Joint.and, this.join.getUniquePath() + Condition.PATH_SEPARATOR + prop);
		}

		@SafeVarargs
		public final Criteria<T> asc(SFunction<R, ?>... lambda) {
			if (lambda == null || lambda.length == 0) {
				return this.criteria;
			}
//			Criteria<T> criteria = this.criteria.copy();
			for (SFunction<R, ?> function : lambda) {
				String prop = LambdaUtils.getPropFromLambda(function);
				String sortProp = this.join.getUniquePath() + Condition.PATH_SEPARATOR + prop;
				criteria.sort(new SortProperty(sortProp, Sort.ASC));
			}
			return criteria;
		}

		@SafeVarargs
		public final Criteria<T> desc(SFunction<R, ?>... lambda) {
			if (lambda == null || lambda.length == 0) {
				return this.criteria;
			}
//			Criteria<T> criteria = this.criteria.copy();
			for (SFunction<R, ?> function : lambda) {
				String prop = LambdaUtils.getPropFromLambda(function);
				String sortProp = this.join.getUniquePath() + Condition.PATH_SEPARATOR + prop;
				criteria.sort(new SortProperty(sortProp, Sort.DESC));
			}
			return criteria;
		}

		public final Criteria<T> then() {
			return criteria;
		}
	}

	/**
	 * 条件构造类
	 *
	 * @param <T> 对象类型
	 */
	public static class ConditionSpec<T, R> {
		/**
		 * 对象属性名，"." 代表级联属性
		 */
		private final String property;
		/**
		 * 关联的criteria
		 */
		private final Criteria<T> criteria;
		/**
		 * 条件的连接符
		 */
		private final Joint joint;

		public ConditionSpec(Criteria<T> criteria, Joint joint, String property) {
			this.property = property;
			this.criteria = criteria;
			this.joint = joint;
		}

		public Criteria<T> beginWith(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
			Condition condition = new Condition(property, Operator.beginWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notBeginWith(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.notBeginWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> contains(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.contains, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notContains(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.notContains, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> endWith(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.endWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notEndWith(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.notEndWith, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> between(@NonNull R start, @NonNull R end) {
			boolean startIsNull = isNullValue(start);
			boolean endIsNull = isNullValue(end);
			if (startIsNull && endIsNull) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("start value[" + start + "] and end value[" + end + "] for property[" + this.property + "] is empty");
				}
			} else if (startIsNull) {
				if (criteria.lenient) {
					Condition condition = new Condition(property, Operator.lessEqual, end);
					condition.setJoint(joint);
					criteria.criterion(condition);
					return criteria;
				} else {
					throw new IllegalArgumentException("start value[" + start + "] for property[" + this.property + "] is empty");
				}
			} else if (endIsNull) {
				if (criteria.lenient) {
					Condition condition = new Condition(property, Operator.greaterEqual, start);
					condition.setJoint(joint);
					criteria.criterion(condition);
					return criteria;
				} else {
					throw new IllegalArgumentException("end value[" + start + "] for property[" + this.property + "] is empty");
				}
			} else {
				Condition condition = new Condition(property, Operator.between, new Object[]{start, end});
				condition.setJoint(joint);
				criteria.criterion(condition);
				return criteria;
			}
		}

		public Criteria<T> notBetween(@NonNull R start, @NonNull R end) {
			boolean startIsNull = isNullValue(start);
			boolean endIsNull = isNullValue(end);
			if (startIsNull && endIsNull) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("start value[" + start + "] and end value[" + end + "] for property[" + this.property + "] is empty");
				}
			} else if (startIsNull) {
				if (criteria.lenient) {
					Condition condition = new Condition(property, Operator.greaterThan, end);
					condition.setJoint(joint);
					criteria.criterion(condition);
					return criteria;
				} else {
					throw new IllegalArgumentException("start value[" + start + "] for property[" + this.property + "] is empty");
				}
			} else if (endIsNull) {
				if (criteria.lenient) {
					Condition condition = new Condition(property, Operator.lessThan, start);
					condition.setJoint(joint);
					criteria.criterion(condition);
					return criteria;
				} else {
					throw new IllegalArgumentException("end value[" + start + "] for property[" + this.property + "] is empty");
				}
			} else {
				Condition condition = new Condition(property, Operator.notBetween, new Object[]{start, end});
				condition.setJoint(joint);
				criteria.criterion(condition);
				return criteria;
			}
		}

		public Criteria<T> blank() {
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.blank, null);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notBlank() {
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.notBlank, null);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> is(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.equal, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> isNot(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.notEqual, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> greaterThan(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.greaterThan, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> greaterEqual(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.greaterEqual, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> lessEqual(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.lessEqual, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> lessThan(@NonNull R value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.lessThan, value);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> isNull() {
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.isNull, null);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> isNotNull() {
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.isNotNull, null);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(@NonNull Collection<R> values) {
			if (isNullValue(values)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + values + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> in(@NonNull R[] values) {
			if (isNullValue(values)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + values + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.in, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(@NonNull Collection<R> values) {
			if (isNullValue(values)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + values + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> notIn(@NonNull R[] values) {
			if (isNullValue(values)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + values + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
			Condition condition = new Condition(property, Operator.notIn, values);
			condition.setJoint(joint);
			criteria.criterion(condition);
			return criteria;
		}

		public Criteria<T> custom(@NonNull String value) {
			if (isNullValue(value)) {
				if (criteria.lenient) {
					return criteria;
				} else {
					throw new IllegalArgumentException("value[" + value + "] for property[" + this.property + "] is empty");
				}
			}
//			Criteria<T> criteria = this.criteria.copy();
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
			return new CascadeProperty<>(this.cascadeProperty + Condition.PATH_SEPARATOR + property, this.cascadeProperty);
		}

		public <S> CascadeProperty<T, S> next(CFunction<R, S> lambda) {
			String property = LambdaUtils.getPropFromLambda(lambda);
			return new CascadeProperty<>(this.cascadeProperty + Condition.PATH_SEPARATOR + property, this.cascadeProperty);
		}
	}

	private static boolean isNullValue(Object value) {
		if (value == null) {
//			throw new IllegalStateException("value is null");
			return true;
		}
		if (value instanceof String) {
			if (StringUtils.isBlank((CharSequence) value)) {
//				throw new IllegalStateException("value" + value + " is null");
				return true;
			}
		}
		if (value instanceof Collection<?>) {
			if (((Collection<?>) value).isEmpty()) {
//				throw new IllegalStateException("value" + value + " is empty collection");
				return true;
			}
			return ((Collection<?>) value).stream().anyMatch(Criteria::isNullValue);
		}
		if (value.getClass().isArray()) {
			//check arrays
			String simpleName = value.getClass().getSimpleName();
			switch (simpleName) {
				case "int[]": {
					int[] values = (int[]) value;
					if (values.length == 0) {
//						throw new IllegalStateException("value" + value + " is empty array");
						return true;
					}
					break;
				}
				case "long[]": {
					long[] values = (long[]) value;
					if (values.length == 0) {
//						throw new IllegalStateException("value" + value + " is empty array");
						return true;
					}
					break;
				}
				case "char[]": {
					char[] values = (char[]) value;
					if (values.length == 0) {
//						throw new IllegalStateException("value" + value + " is empty array");
						return true;
					}
					break;
				}
				case "double[]": {
					double[] values = (double[]) value;
					if (values.length == 0) {
//						throw new IllegalStateException("value" + value + " is empty array");
						return true;
					}
					break;
				}
				case "byte[]": {
					byte[] values = (byte[]) value;
					if (values.length == 0) {
//						throw new IllegalStateException("value" + value + " is empty array");
						return true;
					}
					break;
				}
				case "short[]": {
					short[] values = (short[]) value;
					if (values.length == 0) {
//						throw new IllegalStateException("value" + value + " is empty array");
						return true;
					}
					break;
				}
				case "boolean[]": {
					boolean[] values = (boolean[]) value;
					if (values.length == 0) {
//						throw new IllegalStateException("value" + value + " is empty array");
						return true;
					}
					break;
				}
				case "float[]": {
					float[] values = (float[]) value;
					if (values.length == 0) {
//						throw new IllegalStateException("value" + value + " is empty array");
						return true;
					}
					break;
				}
				default: {
					Object[] values = (Object[]) value;
					if (values.length == 0) {
//						throw new IllegalStateException("value" + value + " is empty array");
						return true;
					}
					break;
				}
			}
		}
		return false;
	}
}
