package org.loed.framework.common.orm;

import org.loed.framework.common.query.Pagination;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.Selector;

import java.io.Serializable;
import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 12-2-28 下午6:00
 */

public interface BaseDao<T> {
	/**
	 * 保存对象
	 *
	 * @param entity 实体对象
	 */
	void save(T entity);

	/**
	 * 批量保存实体
	 *
	 * @param objectList 实体对象列表
	 * @param batchSize  批量数
	 */
	void batchSave(List<T> objectList, int batchSize);

	/**
	 * 修改对象
	 *
	 * @param entity 实体对象
	 */
	void update(T entity);

	/**
	 * 根据主键删除
	 *
	 * @param id 主键
	 */
	void delete(String id);

	/**
	 * 删除对象
	 *
	 * @param entity 实体对象
	 */
	void delete(T entity);

	/**
	 * 根据主键查询(用于将对象持久化)
	 *
	 * @param id 主键
	 * @return 持久化对象
	 */
	T get(Serializable id);

	/**
	 * 根据主键查找，并且锁定当前记录(用于将对象持久化)
	 *
	 * @param id 主键
	 * @return 返回持久化状态的对象
	 */
	T loadForUpdate(Serializable id);

	/**
	 * 根据主键、属性字段选择器查找(仅用于查询，结果集已经处于脱管状态)
	 *
	 * @param id 主键
	 * @return 托管状态的对象
	 */
	T load(Serializable id);

	/**
	 * 根据主键、属性字段选择器查找(仅用于查询，结果集已经处于脱管状态)
	 *
	 * @param id       主键
	 * @param selector 属性选择器
	 * @return 托管状态的对象
	 */
	T load(Serializable id, Selector selector);

	/**
	 * 根据查询条件类自动分页查询
	 *
	 * @param criteria 查询对象
	 * @param params   参数
	 * @return 查询结果
	 */
	Pagination<T> findPage(Pagination<T> criteria, Object... params);

	/**
	 * 根据条件自动查询
	 *
	 * @param criteria 查询条件
	 * @return 查询结果集， 结果集处于持久化状态
	 */
	List<T> queryByCriteria(Criteria criteria);

	/**
	 * 根据条件自动查询
	 *
	 * @param criteria 查询条件
	 * @return 查询结果集， 结果集处于托管状态
	 */
	List<T> findByCriteria(Criteria criteria);

	/**
	 * 调用存储过程
	 *
	 * @param procedureName 存储过程的名称
	 * @param args          存储过程的参数
	 * @return 返回结果
	 */
	<X> List<X> callProcedure(String procedureName, Object... args);

	/**
	 * 检查单一某个属性是否重复，排除传入的主键的数据
	 *
	 * @param id        主键
	 * @param propName  属性名
	 * @param propValue 属性值
	 * @return 是否重复
	 */
	boolean checkPropRepeat(String id, String propName, Object propValue);

	/**
	 * 根据属性查询对象 常用于多对一的情况下根据外键查询一条记录
	 *
	 * @param propertyName 属性名称
	 * @param value        属性值
	 * @return T
	 */
	T findByProperty(String propertyName, Object value);

	/**
	 * 根据属性查询对象 常用于一对多的情况下根据外键查询多条记录
	 *
	 * @param propertyName 属性名
	 * @param value        属性值
	 * @return List
	 */
	List<T> findListByProperty(String propertyName, Object value);
}
