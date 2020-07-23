package org.loed.framework.jdbc;

import org.loed.framework.common.query.Pagination;
import org.loed.framework.common.po.BasePO;
import org.loed.framework.common.query.Criteria;

import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-27 下午8:57
 */
public interface BaseDao<T extends BasePO> {
	int insert(T entity);

	int batchInsert(List<T> entityList);

	void update(T entity);

	void updateSelective(T entity);

	int delete(String id);

	T get(String id);

	List<T> findByProperty(String propName, Object propValue);

	List<T> findByCriteria(Criteria criteria);

	Pagination<T> findPageByProperty(Pagination<T> pagination, String propName, Object propValue);

	Pagination<T> findPageByCriteria(Pagination<T> pagination, Criteria criteria);
}
