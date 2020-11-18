package org.loed.framework.mybatis;

import org.apache.ibatis.annotations.*;
import org.loed.framework.common.ConfigureConstant;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.query.Criteria;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/11/6 12:10 下午
 */
public interface MybatisOperations<T> {
	String INSERT = ConfigureConstant.MYBATIS_NS + ".insert";
	String BATCH_INSERT = ConfigureConstant.MYBATIS_NS + ".batchInsert";
	String BATCH_UPDATE_FIXED = ConfigureConstant.MYBATIS_NS + ".batchUpdateFixed";
	String BATCH_UPDATE_DYNAMICALLY = ConfigureConstant.MYBATIS_NS + ".batchUpdateDynamically";

	@Update(BATCH_UPDATE_FIXED)
	int _batchUpdate(@Param("list") List<T> poList, @Param("predicate") Predicate<Column> predicate);

	@Update(BATCH_UPDATE_DYNAMICALLY)
	int _batchUpdateNonBlank(@Param("list") List<T> poList, @Param("predicate") Predicate<Column> predicate);

	@Insert(INSERT)
	int _insert(T po);

	@Insert(BATCH_INSERT)
	int _batchInsert(@Param("list") List<T> poList, @Param("map") Map<String, Object> map);

	@UpdateProvider(type = MybatisSqlBuilder.class, method = "update")
	int _update(@Param("po") T po, @Param("columnFilter") Predicate<Column> predicate);

	/*******************************按照查询条件删除********************************/
	@DeleteProvider(type = MybatisSqlBuilder.class, method = "deleteByCriteria")
	int _deleteByCriteria(@Param("clazz") Class<T> clazz, @Param("criteria") Criteria<T> criteria, @Param("map") Map<String, Object> map);

	/********************************对象查询方法****************************/
	@SelectProvider(type = MybatisSqlBuilder.class, method = "findByCriteria")
	List<T> _findByCriteria(@Param("clazz") Class<T> clazz, @Param("criteria") Criteria<T> criteria, @Param("map") Map<String, Object> map);

	@SelectProvider(type = MybatisSqlBuilder.class, method = "countByCriteria")
	Long _countByCriteria(@Param("clazz") Class<T> clazz, @Param("criteria") Criteria<T> criteria, @Param("map") Map<String, Object> map);

	@UpdateProvider(type = MybatisSqlBuilder.class, method = "sql")
	int _execute(@Param("sql") String sql, @Param("params") Map<String, Object> params);

	@SelectProvider(type = MybatisSqlBuilder.class, method = "sql")
	List<Map<String, Object>> _query(@Param("sql") String sql, @Param("params") Map<String, Object> params);

}
