package org.loed.framework.mybatis;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Filters;
import org.loed.framework.common.orm.ORMapping;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.query.*;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.mybatis.listener.MybatisListenerContainer;
import org.loed.framework.mybatis.listener.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * todo remove cache here
 *
 * @author thomason
 * @version 1.0
 * @since 2017/8/2 下午3:45
 */
@SuppressWarnings({"unchecked", "Duplicates"})
public interface BaseMapper<T, ID extends Serializable> extends MybatisOperations<T> {
	Logger logger = LoggerFactory.getLogger(BaseMapper.class);

	/*******************************插入方法********************************/
	default int insert(T po) {
		//pre insert listener 处理
		List<PreInsertListener> preInsertListeners = MybatisListenerContainer.getPreInsertListeners();
		if (CollectionUtils.isNotEmpty(preInsertListeners)) {
			for (PreInsertListener preInsertListener : preInsertListeners) {
				boolean execute = preInsertListener.preInsert(po);
				if (!execute) {
					return 0;
				}
			}
		}
		int rows = _insert(po);
		//post insert listener 处理
		List<PostInsertListener> postInsertListeners = MybatisListenerContainer.getPostInsertListeners();
		if (CollectionUtils.isNotEmpty(postInsertListeners)) {
			postInsertListeners.forEach(postInsertListener -> postInsertListener.postInsert(po));
		}
		return rows;
	}

	default int batchInsert(@Param("list") List<T> poList) {
		if (poList == null || poList.size() == 0) {
			return 0;
		}
		List<PreInsertListener> preInsertListeners = MybatisListenerContainer.getPreInsertListeners();
		//pre insert listener 处理
		if (CollectionUtils.isNotEmpty(preInsertListeners)) {
			for (PreInsertListener preInsertListener : preInsertListeners) {
				boolean execute = preInsertListener.preInsert(poList);
				if (!execute) {
					return 0;
				}
			}
		}
		int rows = _batchInsert(poList, new HashMap<>());
		//post insert listener 处理
		List<PostInsertListener> postInsertListeners = MybatisListenerContainer.getPostInsertListeners();
		if (CollectionUtils.isNotEmpty(postInsertListeners)) {
			postInsertListeners.forEach(postInsertListener -> postInsertListener.postInsert(poList));
		}
		return rows;
	}

	default int _update(T po, Predicate<Column> predicate, List<Condition> conditions) {
		boolean b = _preUpdate(Collections.singletonList(po));
		if (!b) {
			logger.warn("preupdate false ,will not update");
			return 0;
		}

		int rows = _update(po, predicate.and(Filters.UPDATABLE_FILTER));

		//post update listener 处理
		_postUpdate(Collections.singletonList(po));
		return rows;
	}

	default int update(T po) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Column id = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		if (id == null) {
			throw new RuntimeException("not find @Id column in object:" + entityClass.getName());
		}
		Object idValue = ReflectionUtils.getFieldValue(po, id.getJavaName());
		if (idValue == null) {
			throw new RuntimeException("@Id is null from object:" + po);
		}
		List<Condition> conditions = new ArrayList<>();
		conditions.add(new Condition(id.getJavaName(), Operator.equal, idValue));
		return _update(po, Filters.ALWAYS_TRUE_FILTER, _mergeWithCommonConditions(conditions));
	}

	default int updateWith(T po, SFunction<T, ?>... props) {
		if (props == null || props.length == 0) {
			throw new RuntimeException("can't update empty columns");
		}
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Column id = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		if (id == null) {
			throw new RuntimeException("not find @Id column in object:" + entityClass.getName());
		}
		Object idValue = ReflectionUtils.getFieldValue(po, id.getJavaName());
		if (idValue == null) {
			throw new RuntimeException("@Id is null from object:" + po);
		}
		List<Condition> conditions = new ArrayList<>();
		conditions.add(new Condition(id.getJavaName(), Operator.equal, idValue));
		List<String> includes = Arrays.stream(props).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		Predicate<Column> predicate = new Filters.IncludeFilter(includes);
		return _update(po, predicate, conditions);
	}

	default int updateWithout(T po, SFunction<T, ?>... props) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Column id = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		if (id == null) {
			throw new RuntimeException("not find @Id column in object:" + entityClass.getName());
		}
		Object idValue = ReflectionUtils.getFieldValue(po, id.getJavaName());
		if (idValue == null) {
			throw new RuntimeException("@Id is null from object:" + po);
		}
		List<Condition> conditions = new ArrayList<>();
		conditions.add(new Condition(id.getJavaName(), Operator.equal, idValue));
		Predicate<Column> predicate = Filters.ALWAYS_TRUE_FILTER;
		if (props != null && props.length > 0) {
			List<String> includes = Arrays.stream(props).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
			predicate = new Filters.ExcludeFilter(includes);
		}
		return _update(po, predicate, conditions);
	}

	@Deprecated
	default int updateNonBlank(T po) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Column id = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		if (id == null) {
			throw new RuntimeException("not find @Id column in object:" + entityClass.getName());
		}
		Object idValue = ReflectionUtils.getFieldValue(po, id.getJavaName());
		if (idValue == null) {
			throw new RuntimeException("@Id is null from object:" + po);
		}
		List<Condition> conditions = new ArrayList<>();
		conditions.add(new Condition(id.getJavaName(), Operator.equal, idValue));
		return _update(po, new Filters.NonBlankFilter(po), conditions);
	}

	default int updateNonNull(T po) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Column id = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		if (id == null) {
			throw new RuntimeException("not find @Id column in object:" + entityClass.getName());
		}
		Object idValue = ReflectionUtils.getFieldValue(po, id.getJavaName());
		if (idValue == null) {
			throw new RuntimeException("@Id is null from object:" + po);
		}
		List<Condition> conditions = new ArrayList<>();
		conditions.add(new Condition(id.getJavaName(), Operator.equal, idValue));
		return _update(po, new Filters.NonNullFilter(po), conditions);
	}

	@Deprecated
	default int updateNonBlankAnd(T po, SFunction<T, ?>... props) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Column id = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		if (id == null) {
			throw new RuntimeException("not find @Id column in object:" + entityClass.getName());
		}
		Object idValue = ReflectionUtils.getFieldValue(po, id.getJavaName());
		if (idValue == null) {
			throw new RuntimeException("@Id is null from object:" + po);
		}
		List<Condition> conditions = new ArrayList<>();
		conditions.add(new Condition(id.getJavaName(), Operator.equal, idValue));
		Predicate<Column> predicate = new Filters.NonBlankFilter(po);
		if (props != null && props.length > 0) {
			List<String> includes = Arrays.stream(props).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
			predicate = predicate.or(new Filters.IncludeFilter(includes));
		}
		return _update(po, predicate, conditions);
	}

	@Deprecated
	default int updateNonNullAnd(T po, SFunction<T, ?>... props) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Column id = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		if (id == null) {
			throw new RuntimeException("not find @Id column in object:" + entityClass.getName());
		}
		Object idValue = ReflectionUtils.getFieldValue(po, id.getJavaName());
		if (idValue == null) {
			throw new RuntimeException("@Id is null from object:" + po);
		}
		List<Condition> conditions = new ArrayList<>();
		conditions.add(new Condition(id.getJavaName(), Operator.equal, idValue));
		Predicate<Column> predicate = new Filters.NonNullFilter(po);
		if (props != null && props.length > 0) {
			List<String> includes = Arrays.stream(props).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
			predicate = predicate.or(new Filters.IncludeFilter(includes));
		}
		return _update(po, predicate, conditions);
	}

	default int batchUpdate(List<T> poList) {
		if (poList == null || poList.size() == 0) {
			return 0;
		}
		boolean b = _preUpdate(poList);
		if (!b) {
			logger.warn("empty rows to update");
			return 0;
		}
		int i = _batchUpdate(poList, Filters.ALWAYS_TRUE_FILTER.and(Filters.UPDATABLE_FILTER));
		_postUpdate(poList);
		return i;
	}

	default int batchUpdateWith(List<T> poList, SFunction<T, ?>... props) {
		if (props == null || props.length == 0) {
			throw new RuntimeException("can't update empty columns");
		}
		if (poList == null || poList.size() == 0) {
			return 0;
		}
		List<String> includes = Arrays.stream(props).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		boolean b = _preUpdate(poList);
		if (!b) {
			logger.warn("empty rows to update");
			return 0;
		}
		int i = _batchUpdate(poList, new Filters.IncludeFilter(includes).and(Filters.UPDATABLE_FILTER));
		_postUpdate(poList);
		return i;
	}

	default int batchUpdateWithOut(List<T> poList, SFunction<T, ?>... props) {
		if (poList == null || poList.size() == 0) {
			return 0;
		}
		Predicate<Column> predicate = null;
		if (props != null && props.length > 0) {
			List<String> includes = Arrays.stream(props).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
			predicate = new Filters.ExcludeFilter(includes);
		} else {
			predicate = Filters.ALWAYS_TRUE_FILTER;
		}
		boolean b = _preUpdate(poList);
		if (!b) {
			logger.warn("empty rows to update");
			return 0;
		}
		int i = _batchUpdate(poList, predicate.and(Filters.UPDATABLE_FILTER));
		_postUpdate(poList);
		return i;
	}

	@Deprecated
	default int batchUpdateNonBlank(List<T> poList) {
		if (poList == null || poList.size() == 0) {
			return 0;
		}
		boolean b = _preUpdate(poList);
		if (!b) {
			logger.warn("empty rows to update");
			return 0;
		}

		int rows = _batchUpdateNonBlank(poList, Filters.UPDATABLE_FILTER);

		//post insert listener 处理
		_postUpdate(poList);
		return rows;
	}

	default int batchUpdateNonNull(List<T> poList) {
		if (poList == null || poList.size() == 0) {
			return 0;
		}
		boolean b = _preUpdate(poList);
		if (!b) {
			logger.warn("empty rows to update");
			return 0;
		}

		int rows = _batchUpdateNonNull(poList, Filters.UPDATABLE_FILTER);

		//post insert listener 处理
		_postUpdate(poList);
		return rows;
	}

	default boolean _preUpdate(List<T> poList) {
		List<PreUpdateListener> preUpdateListeners = MybatisListenerContainer.getPreUpdateListeners();
		if (CollectionUtils.isNotEmpty(preUpdateListeners)) {
			for (PreUpdateListener preInsertListener : preUpdateListeners) {
				boolean result = true;
				for (T t : poList) {
					boolean execute = preInsertListener.preUpdate(t);
					result = result && execute;
				}
				if (!result) {
					return false;
				}
			}
		}
		return true;
	}

	;

	default void _postUpdate(List<T> poList) {
		List<PostUpdateListener> postUpdateListeners = MybatisListenerContainer.getPostUpdateListeners();
		if (CollectionUtils.isNotEmpty(postUpdateListeners)) {
			for (PostUpdateListener preInsertListener : postUpdateListeners) {
				boolean result = true;
				for (T t : poList) {
					preInsertListener.postUpdate(t);
				}

			}
		}
	}

	/**
	 * 按照主键删除对象
	 *
	 * @param id 主键
	 * @return 影响的行数
	 */
	default int delete(ID id) {
		T po = null;
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		//pre postDelete listener 处理
		List<PreDeleteListener> preDeleteListeners = MybatisListenerContainer.getPreDeleteListeners();
		if (CollectionUtils.isNotEmpty(preDeleteListeners)) {
			po = get(id);
			if (po == null) {
				return 0;
			}
			for (PreDeleteListener preInsertListener : preDeleteListeners) {
				boolean execute = preInsertListener.preDelete(po);
				if (!execute) {
					return 0;
				}
			}
		}

		int rows = _deleteByCriteria(entityClass, _fromId(id), new HashMap<>());

		List<PostDeleteListener> postDeleteListeners = MybatisListenerContainer.getPostDeleteListeners();
		if (CollectionUtils.isNotEmpty(postDeleteListeners)) {
			if (po == null) {
				return 0;
			}
			for (PostDeleteListener postDeleteListener : postDeleteListeners) {
				postDeleteListener.postDelete(po);
			}
		}
		return rows;
	}

//	default List<T> getList(List<ID> idList) {
//		if (CollectionUtils.isEmpty(idList)) {
//			return new ArrayList<>();
//		}
//		Criteria<T> criteria = _fromIdList(idList);
//		return findByCriteria(criteria);
//	}

//	@Select(value = BatchType.BatchGetByIdList.name())
//	List<T> _shardingByIdList(@Param("idList") List<ID> idList, @Param("clazz") Class<T> clazz);
//
//	default List<T> getByIdList(List<ID> idList) {
//		if (CollectionUtils.isEmpty(idList)) {
//			return new ArrayList<>();
//		}
//		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
//		Table table = ORMapping.get(entityClass);
//		if (table.isSharding()) {
//			return _shardingByIdList(idList, entityClass);
//		}
//		Criteria<T> criteria = _fromIdList(idList);
//		return _findByCriteria(entityClass, criteria, new HashMap<>());
//	}

	default int delete(Criteria<T> criteria) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		List<Condition> conditions = _mergeWithCommonConditions(criteria.getConditions());
		criteria.setConditions(conditions);
		return _deleteByCriteria(entityClass, criteria, new HashMap<>());
	}

	default T get(ID id) {
		return findOne(_fromId(id));
	}

	default T getById(ID id) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		List<T> list = _findByCriteria(entityClass, _fromId(id), new HashMap<>());
		return list.size() == 0 ? null : list.get(0);
	}

	default int deleteById(ID id) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		return _deleteByCriteria(entityClass, _fromId(id), new HashMap<>());
	}

	default <R> List<T> find(SFunction<T, R> propName, R propValue) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Criteria<T> criteria = Criteria.from(entityClass);
		return find(criteria.and(propName).is(propValue));
	}

	default <R> T findOne(SFunction<T, R> propName, R propValue) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Criteria<T> criteria = Criteria.from(entityClass);
		return findOne(criteria.and(propName).is(propValue));
	}


	default List<T> find(Criteria<T> criteria) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		List<Condition> conditions = _mergeWithCommonConditions(criteria.getConditions());
		criteria.setConditions(conditions);
		return _findByCriteria(entityClass, criteria, new HashMap<>());
	}

	default T findOne(Criteria<T> criteria) {
		PageHelper.startPage(1, 1, false);
		List<T> list = find(criteria);
		return list.size() == 0 ? null : list.get(0);
	}

	/**
	 * 分页查询对象
	 *
	 * @param request 查询条件
	 * @return 分页查询结果
	 */
	default Pagination<T> findPage(PageRequest request, Criteria<T> criteria) {
		if (request.isPaging()) {
			if (request.isCounting()) {
				PageHelper.startPage(request.getPageNumber(), request.getPageSize(), true);
			} else {
				PageHelper.startPage(request.getPageNumber(), request.getPageSize(), false);
			}
		}
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		List<T> list = this.find(criteria == null ? Criteria.from(entityClass) : criteria);
		PageInfo<T> pageInfo = new PageInfo<>(list);
		Pagination<T> response = new Pagination<>();
		response.setTotal(pageInfo.getTotal());
		response.setRows(pageInfo.getList());
		response.setPageSize(request.getPageSize());
		response.setPageNo(request.getPageNumber());
		return response;
	}

	default Long count(Criteria<T> criteria) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		List<Condition> conditions = _mergeWithCommonConditions(criteria.getConditions());
		criteria.setConditions(conditions);
		return _countByCriteria(entityClass, criteria, new HashMap<>());
	}

	default <R> boolean checkRepeat(ID id, SFunction<T, R> lambda, R propValue) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Criteria<T> criteria = Criteria.from(entityClass).and(lambda).is(propValue);

		Optional<Column> idColumn = table.getColumns().stream().filter(Column::isPk).findAny();
		if (!idColumn.isPresent()) {
			throw new RuntimeException("entity class:" + entityClass.getName() + " has no id column");
		}
		String javaName = idColumn.get().getJavaName();
		Condition condition = new Condition();
		condition.setPropertyName(javaName);
		condition.setOperator(Operator.notEqual);
		condition.setValue(id);

		criteria.getConditions().add(condition);
		Long count = count(criteria);
		return count > 0;
	}

	default int execute(String sql, Map<String, Object> params) {
		return _execute(sql, params);
	}


	default List<Map<String, Object>> query(String sql, Map<String, Object> params) {
		return _query(sql, params);
	}


	default Criteria<T> _fromId(ID id) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Criteria<T> criteria = Criteria.from(entityClass);
		Optional<Column> idColumn = table.getColumns().stream().filter(Column::isPk).findAny();
		if (!idColumn.isPresent()) {
			throw new RuntimeException("entity class:" + entityClass.getName() + " has no id column");
		}
		String javaName = idColumn.get().getJavaName();
		Condition condition = new Condition();
		condition.setPropertyName(javaName);
		condition.setOperator(Operator.equal);
		condition.setValue(id);
		criteria.getConditions().add(condition);
		return criteria;
	}

	default Criteria<T> _fromIdList(List<ID> idList) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Criteria<T> criteria = Criteria.from(entityClass);
		Optional<Column> idColumn = table.getColumns().stream().filter(Column::isPk).findAny();
		if (!idColumn.isPresent()) {
			throw new RuntimeException("entity class:" + entityClass.getName() + " has no id column");
		}
		String javaName = idColumn.get().getJavaName();
		Condition condition = new Condition();
		condition.setPropertyName(javaName);
		condition.setOperator(Operator.in);
		condition.setValue(idList);
		criteria.getConditions().add(condition);
		return criteria;
	}

	default List<Condition> _mergeWithCommonConditions(List<Condition> conditions) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		Optional<Column> isDeleted = table.getColumns().stream().filter(Column::isDeleted).findAny();
		if (isDeleted.isPresent()) {
			Column column = isDeleted.get();
			Condition condition = new Condition();
			condition.setPropertyName(column.getJavaName());
			condition.setOperator(Operator.equal);
			Class<?> type = column.getJavaType();
			if (type.getName().equals(Integer.class.getName()) || type.getName().equals(int.class.getName())) {
				condition.setValue(0);
			} else if (type.getName().equals(Byte.class.getName()) || type.getName().equals(byte.class.getName())) {
				condition.setValue((byte) 0);
			}
			conditions.add(condition);
		}

		Optional<Column> tenantId = table.getColumns().stream().filter(Column::isTenantId).findAny();
		if (tenantId.isPresent()) {
			Column column = tenantId.get();
			Condition condition = new Condition();
			condition.setPropertyName(column.getJavaName());
			condition.setOperator(Operator.equal);
			condition.setValue(SystemContextHolder.getTenantId());
			conditions.add(condition);
		}
		return conditions;
	}
}
