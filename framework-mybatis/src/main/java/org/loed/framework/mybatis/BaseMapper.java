package org.loed.framework.mybatis;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.*;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.po.BasePO;
import org.loed.framework.common.po.Identify;
import org.loed.framework.common.query.Condition;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.Operator;
import org.loed.framework.common.query.Pagination;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.mybatis.listener.MybatisListenerContainer;
import org.loed.framework.mybatis.listener.spi.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.persistence.GenerationType;
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
public interface BaseMapper<T, ID extends Serializable> {

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

	@InsertProvider(type = MybatisSqlBuilder.class, method = "insert")
	int _insert(T po);

	/*******************************批量插入方法********************************/
	@InsertProvider(type = MybatisSqlBuilder.class, method = "batchInsert")
	int _batchInsert(@Param("list") List<T> poList, @Param("map") Map<String, Object> map);

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

	/*******************************全部更新方法********************************/
	@UpdateProvider(type = MybatisSqlBuilder.class, method = "update")
	int _update(@Param("po") T po, @Param("columnFilter") Predicate<Column> includes);

	default int update(T po) {
		List<PreUpdateListener> preUpdateListeners = MybatisListenerContainer.getPreUpdateListeners();
		//pre update listener 处理
		if (CollectionUtils.isNotEmpty(preUpdateListeners)) {
			for (PreUpdateListener preInsertListener : preUpdateListeners) {
				boolean execute = preInsertListener.preUpdate(po);
				if (!execute) {
					return 0;
				}
			}
		}
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];

		int rows = _update(po, UPDATABLE_FILTER);

		//post update listener 处理
		List<PostUpdateListener> postUpdateListeners = MybatisListenerContainer.getPostUpdateListeners();
		if (CollectionUtils.isNotEmpty(postUpdateListeners)) {
			postUpdateListeners.forEach(postUpdateListener -> postUpdateListener.postUpdate(po));
		}
		return rows;
	}

	default int updateWith(T po, SFunction<T, ?>... includes) {
		List<PreUpdateListener> preUpdateListeners = MybatisListenerContainer.getPreUpdateListeners();
		//pre update listener 处理
		if (CollectionUtils.isNotEmpty(preUpdateListeners)) {
			for (PreUpdateListener preInsertListener : preUpdateListeners) {
				boolean execute = preInsertListener.preUpdate(po);
				if (!execute) {
					return 0;
				}
			}
		}
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Predicate<Column> predicate = UPDATABLE_FILTER;
		if (includes != null && includes.length > 0) {
			Set<String> includeProps = Arrays.stream(includes).map(LambdaUtils::getPropFromLambda).collect(Collectors.toSet());
			predicate = predicate.and(new IncludeFilter(includeProps));
		}
		int rows = _update(po, predicate);

		//post update listener 处理
		List<PostUpdateListener> postUpdateListeners = MybatisListenerContainer.getPostUpdateListeners();
		if (CollectionUtils.isNotEmpty(postUpdateListeners)) {
			postUpdateListeners.forEach(postUpdateListener -> postUpdateListener.postUpdate(po));
		}
		return rows;
	}

	default int updateWithout(T po, SFunction<T, ?>... excludes) {
		List<PreUpdateListener> preUpdateListeners = MybatisListenerContainer.getPreUpdateListeners();
		//pre update listener 处理
		if (CollectionUtils.isNotEmpty(preUpdateListeners)) {
			for (PreUpdateListener preInsertListener : preUpdateListeners) {
				boolean execute = preInsertListener.preUpdate(po);
				if (!execute) {
					return 0;
				}
			}
		}
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Predicate<Column> predicate = UPDATABLE_FILTER;
		if (excludes != null && excludes.length > 0) {
			Set<String> includeProps = Arrays.stream(excludes).map(LambdaUtils::getPropFromLambda).collect(Collectors.toSet());
			predicate = predicate.and(new ExcludeFilter(includeProps));
		}
		int rows = _update(po, predicate);

		//post update listener 处理
		List<PostUpdateListener> postUpdateListeners = MybatisListenerContainer.getPostUpdateListeners();
		if (CollectionUtils.isNotEmpty(postUpdateListeners)) {
			postUpdateListeners.forEach(postUpdateListener -> postUpdateListener.postUpdate(po));
		}
		return rows;
	}

	default int updateNonBlank(T po) {
		List<PreUpdateListener> preUpdateListeners = MybatisListenerContainer.getPreUpdateListeners();
		//pre update listener 处理
		if (CollectionUtils.isNotEmpty(preUpdateListeners)) {
			for (PreUpdateListener preInsertListener : preUpdateListeners) {
				boolean execute = preInsertListener.preUpdate(po);
				if (!execute) {
					return 0;
				}
			}
		}
		Set<String> includeColumns = null;

		int rows = _update(po, new NonBlankFilter(po).and(UPDATABLE_FILTER));

		//post update listener 处理
		List<PostUpdateListener> postUpdateListeners = MybatisListenerContainer.getPostUpdateListeners();
		if (CollectionUtils.isNotEmpty(postUpdateListeners)) {
			postUpdateListeners.forEach(postUpdateListener -> postUpdateListener.postUpdate(po));
		}
		return rows;
	}

	default int updateNonBlankAnd(T po, @Nullable SFunction<T, ?>... columns) {
		List<PreUpdateListener> preUpdateListeners = MybatisListenerContainer.getPreUpdateListeners();
		//pre update listener 处理
		if (CollectionUtils.isNotEmpty(preUpdateListeners)) {
			for (PreUpdateListener preInsertListener : preUpdateListeners) {
				boolean execute = preInsertListener.preUpdate(po);
				if (!execute) {
					return 0;
				}
			}
		}
		Set<String> includeColumns = null;
		Predicate<Column> filter = UPDATABLE_FILTER;
		if (columns != null && columns.length > 0) {
			Set<String> includes = Arrays.stream(columns).map(LambdaUtils::getPropFromLambda).collect(Collectors.toSet());
			filter = filter.and(new IncludeFilter(includes).or(new NonBlankFilter(po)));
		} else {
			filter = filter.and(new NonBlankFilter(po));
		}
		int rows = _update(po, filter);

		//post update listener 处理
		List<PostUpdateListener> postUpdateListeners = MybatisListenerContainer.getPostUpdateListeners();
		if (CollectionUtils.isNotEmpty(postUpdateListeners)) {
			postUpdateListeners.forEach(postUpdateListener -> postUpdateListener.postUpdate(po));
		}
		return rows;
	}

	default int batchUpdateNonBlank(List<T> poList) {
		if (poList == null || poList.size() == 0) {
			return 0;
		}
		List<PreUpdateListener> preUpdateListeners = MybatisListenerContainer.getPreUpdateListeners();
		//pre update listener 处理
		if (CollectionUtils.isNotEmpty(preUpdateListeners)) {
			for (PreUpdateListener preInsertListener : preUpdateListeners) {
				boolean result = true;
				for (T t : poList) {
					boolean execute = preInsertListener.preUpdate(t);
					result = result && execute;
				}
				if (!result) {
					return 0;
				}
			}
		}

		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		int rows = 0;
		for (T t : poList) {
			NonBlankFilter nonBlankFilter = new NonBlankFilter(t);
			rows += _update(t, nonBlankFilter.and(UPDATABLE_FILTER));
		}

		//post insert listener 处理
		List<PostUpdateListener> postUpdateListeners = MybatisListenerContainer.getPostUpdateListeners();
		if (CollectionUtils.isNotEmpty(postUpdateListeners)) {
			postUpdateListeners.forEach(postUpdateListener -> poList.forEach(postUpdateListener::postUpdate));
		}
		return rows;
	}

	default int batchUpdate(List<T> poList, String... columns) {
		if (poList == null || poList.size() == 0) {
			return 0;
		}
		List<PreUpdateListener> preUpdateListeners = MybatisListenerContainer.getPreUpdateListeners();
		//pre update listener 处理
		if (CollectionUtils.isNotEmpty(preUpdateListeners)) {
			for (PreUpdateListener preInsertListener : preUpdateListeners) {
				boolean result = true;
				for (T t : poList) {
					boolean execute = preInsertListener.preUpdate(t);
					result = result && execute;
				}
				if (!result) {
					return 0;
				}
			}
		}

		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		int rows = 0;
		for (T t : poList) {
			rows += _update(t, UPDATABLE_FILTER);
		}

		//post insert listener 处理
		List<PostUpdateListener> postUpdateListeners = MybatisListenerContainer.getPostUpdateListeners();
		if (CollectionUtils.isNotEmpty(postUpdateListeners)) {
			postUpdateListeners.forEach(postUpdateListener -> poList.forEach(postUpdateListener::postUpdate));
		}
		return rows;
	}

	/*******************************删除方法********************************/
	@DeleteProvider(type = MybatisSqlBuilder.class, method = "delete")
	int _delete(@Param("table") Table table, @Param("id") ID Id);

	default int delete(ID id) {
		T po = null;
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		//pre postDelete listener 处理
		List<PreDeleteListener> preDeleteListeners = MybatisListenerContainer.getPreDeleteListeners();
		if (CollectionUtils.isNotEmpty(preDeleteListeners)) {
			po = get(id);
			for (PreDeleteListener preInsertListener : preDeleteListeners) {
				boolean execute = preInsertListener.preDelete(po);
				if (!execute) {
					return 0;
				}
			}
		}
		int rows = _delete(ORMapping.get(entityClass), id);
		//post postDelete listener 处理
		List<PostDeleteListener> postDeleteListeners = MybatisListenerContainer.getPostDeleteListeners();
		if (CollectionUtils.isNotEmpty(postDeleteListeners)) {
			if (po == null) {
				po = get(id);
			}
			for (PostDeleteListener postDeleteListener : postDeleteListeners) {
				postDeleteListener.postDelete(po);
			}
		}
		return rows;
	}

	@SelectProvider(type = MybatisSqlBuilder.class, method = "shardingGetList")
	List<T> _shardingGetList(@Param("idList") List<Serializable> idList, @Param("clazz") Class<T> clazz);

	//isdelted == 0
	default List<T> getList(List<Serializable> idList) {
		if (CollectionUtils.isEmpty(idList)) {
			return new ArrayList<>();
		}
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		if (table.isSharding()) {
			return _shardingGetList(idList, entityClass);
		}
		Criteria<T> criteria = Criteria.from(entityClass);
		criteria.and(Identify::getId).in(idList);
		return findByCriteria(criteria);
	}

	@SelectProvider(type = MybatisSqlBuilder.class, method = "shardingGetByIdList")
	List<T> _shardingByIdList(@Param("idList") List<Serializable> idList, @Param("clazz") Class<T> clazz);

	default List<T> getByIdList(List<Serializable> idList) {
		if (CollectionUtils.isEmpty(idList)) {
			return new ArrayList<>();
		}
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Table table = ORMapping.get(entityClass);
		if (table.isSharding()) {
			return _shardingByIdList(idList, entityClass);
		}
		Criteria<T> criteria = Criteria.from(entityClass);
		criteria.and(Identify::getId).in(idList);
		return _findByCriteria(entityClass, criteria, new HashMap<>());
	}

	/*******************************按照查询条件删除********************************/
	@DeleteProvider(type = MybatisSqlBuilder.class, method = "deleteByCriteria")
	int _deleteByCriteria(@Param("clazz") Class<T> clazz, @Param("criteria") Criteria<T> criteria, @Param("map") Map<String, Object> map);

	@UpdateProvider(type = MybatisSqlBuilder.class, method = "updateByCriteria")
	int _updateByCriteria(@Param("clazz") Class<T> clazz, @Param("criteria") Criteria<T> criteria, @Param("columnMap") Map<String, Object> columnMap, @Param("map") Map<String, Object> map);

	default int deleteByCriteria(Criteria<T> criteria) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		if (ReflectionUtils.isSubClass(entityClass, CommonPO.class)) {
			Map<String, Object> columnMap = new HashMap<>();
			columnMap.put("isDeleted", 0);
			columnMap.put("updateBy", SystemContextHolder.getAccountId());
			columnMap.put("updateTime", new Date());
			return _updateByCriteria(entityClass, criteria, columnMap, new HashMap<>());
		} else {
			return _deleteByCriteria(entityClass, criteria, new HashMap<>());
		}
	}

	/*******************************按照主键查询********************************/
	@SelectProvider(type = MybatisSqlBuilder.class, method = "get")
	T _get(@Param("map") Map<String, Object> map, @Param("clazz") Class<T> clazz);

	default T get(Serializable id) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		return _get(map, entityClass);
	}

	default List<T> findByProperty(SFunction<T, ?> propName, Object propValue) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Criteria<T> criteria = Criteria.from(entityClass);
		return findByCriteria(criteria.and(propName).is(propValue));
	}

	default T findOne(SFunction<T, ?> propName, Object propValue) {
		List<T> list = findByProperty(propName, propValue);
		return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
	}

	/********************************对象查询方法****************************/
	@SelectProvider(type = MybatisSqlBuilder.class, method = "findByCriteria")
	List<T> _findByCriteria(@Param("clazz") Class<T> clazz, @Param("criteria") Criteria<T> criteria, @Param("map") Map<String, Object> map);

	default List<T> findByCriteria(Criteria<T> criteria) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		if (ReflectionUtils.isSubClass(entityClass, CommonPO.class)) {
			criteria.getConditions().add(new Condition("isDeleted", Operator.equal, 0));
		}
		if (ReflectionUtils.isSubClass(entityClass, BasePO.class)) {
			criteria.getConditions().add(new Condition("tenantCode", Operator.equal, SystemContextHolder.getTenantCode()));
		}
		return _findByCriteria(entityClass, criteria, new HashMap<>());
	}

	default T findOne(Criteria<T> criteria) {
		PageHelper.startPage(1, 1, false);
		List<T> list = findByCriteria(criteria);
		return list.size() == 0 ? null : list.get(0);
	}

	/**
	 * 分页查询对象
	 *
	 * @param request 查询条件
	 * @return 分页查询结果
	 */
	/*default PageResponse<T> findPage(PageRequest request) {
		if (request.isNeedPaging()) {
			PageHelper.startPage(request.getPageNo(), request.getPageSize(), request.isNeedCount());
		}
		List<T> list = this.findByCriteria(request.getCriteria() == null ? Criteria.create() : request.getCriteria());
		PageInfo<T> pageInfo = new PageInfo<>(list);
		PageResponse<T> response = new PageResponse<>();
		response.setTotalCount(pageInfo.getTotal());
		response.setRows(pageInfo.getList());
		return response;
	}*/

	/**
	 * 分页查询对象
	 *
	 * @param request 查询条件
	 * @return 分页查询结果
	 */
	default Pagination<T> findPage(PageRequest request, Criteria<T> criteria) {
		if (request.isPaged()) {
			PageHelper.startPage(request.getPageNumber() + 1, request.getPageSize());
		}
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		List<T> list = this.findByCriteria(criteria == null ? Criteria.from(entityClass) : criteria);
		PageInfo<T> pageInfo = new PageInfo<>(list);
		Pagination<T> response = new Pagination<>();
		response.setTotal(pageInfo.getTotal());
		response.setRows(pageInfo.getList());
		response.setPageSize(request.getPageSize());
		response.setPageNo(request.getPageNumber());
		return response;
	}

	@SelectProvider(type = MybatisSqlBuilder.class, method = "countByCriteria")
	Long _countByCriteria(@Param("clazz") Class<T> clazz, @Param("criteria") Criteria<T> criteria, @Param("map") Map<String, Object> map);

	default Long countByCriteria(Criteria<T> criteria) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		if (ReflectionUtils.isSubClass(entityClass, CommonPO.class)) {
			criteria.getConditions().add(new Condition("isDeleted", Operator.equal, 0));
		}
		if (ReflectionUtils.isSubClass(entityClass, BasePO.class)) {
			criteria.getConditions().add(new Condition("tenantCode", Operator.equal, SystemContextHolder.getTenantCode()));
		}
		return _countByCriteria(entityClass, criteria, new HashMap<>());
	}

	default boolean checkRepeat(Serializable id, SFunction<T, ?> lamda, Object propValue) {
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getInterfaces()[0].getGenericInterfaces()[0]).getActualTypeArguments()[0];
		Criteria<T> criteria = Criteria.from(entityClass);
		criteria.and(lamda).is(propValue);
		criteria.and(Identify::getId).isNot(propValue);
		Long count = countByCriteria(criteria);
		return count > 0;
	}

	@UpdateProvider(type = MybatisSqlBuilder.class, method = "sql")
	int _execute(@Param("sql") String sql, @Param("params") Map<String, Object> params);

	default int execute(String sql, Map<String, Object> params) {
		return _execute(sql, params);
	}

	@SelectProvider(type = MybatisSqlBuilder.class, method = "sql")
	List<Map<String, Object>> _query(@Param("sql") String sql, @Param("params") Map<String, Object> params);

	default List<Map<String, Object>> query(String sql, Map<String, Object> params) {
		return _query(sql, params);
	}

	/**
	 * 默认过滤器
	 */
	Predicate<Column> ALWAYS_TRUE_FILTER = column -> true;

	/**
	 * 可插入列的过滤器
	 */
	Predicate<Column> INSERTABLE_FILTER = column -> {
		boolean pk = column.isPk();
		if (pk) {
			GenerationType generationType = column.getIdGenerationType();
			if (Objects.equals(generationType, GenerationType.AUTO)) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	};

	/**
	 * 可更新列的过滤器
	 */
	Predicate<Column> UPDATABLE_FILTER = Column::isUpdatable;

	/**
	 * 指定更新列的过滤器
	 */
	class IncludeFilter implements Predicate<Column> {
		private final Collection<String> includes;

		public IncludeFilter(@NonNull Collection<String> includes) {
			this.includes = includes;
		}

		@Override
		public boolean test(Column column) {
			return includes.contains(column.getJavaName());
		}
	}

	/**
	 * 指定忽略列的过滤器
	 */
	class ExcludeFilter implements Predicate<Column> {
		private final Collection<String> excludes;

		public ExcludeFilter(@NonNull Collection<String> excludes) {
			this.excludes = excludes;
		}

		@Override
		public boolean test(Column column) {
			return !excludes.contains(column.getJavaName());
		}
	}

	/**
	 * 动态判断属性为空的过滤器
	 */
	class NonBlankFilter implements Predicate<Column> {
		private final Object object;

		public NonBlankFilter(@NonNull Object object) {
			this.object = object;
		}

		@Override
		public boolean test(Column column) {
			if (column.isPk()) {
				return false;
			}
			if (column.isVersioned()) {
				return false;
			}
			Object value = ReflectionUtils.getFieldValue(object, column.getJavaName());
			if (value == null) {
				return false;
			}
			if (value instanceof String) {
				return StringUtils.isNotBlank((String) value);
			}
			return true;
		}
	}
}
