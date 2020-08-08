package org.loed.framework.r2dbc.dao;

import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.po.IsDeleted;
import org.loed.framework.common.po.TenantId;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.Pagination;
import org.loed.framework.r2dbc.query.R2dbcParam;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/6 7:42 下午
 */
public interface R2dbcDao<T, ID> {
	/**
	 * 新增一个对象
	 *
	 * @param entity 对象
	 * @param <S>    对象的子类
	 * @return 新增后的对象
	 */
	<S extends T> Mono<S> insert(@NonNull S entity);

	/**
	 * 批量新增对象
	 *
	 * @param entities 对象集合
	 * @param <S>      对象的子类
	 * @return 新增后的对象
	 */
	<S extends T> Flux<S> batchInsert(@NonNull Iterable<S> entities);

	/**
	 * 批量新增对象
	 *
	 * @param entityStream 对象流
	 * @param <S>          对象的子类
	 * @return 新增后的对象
	 */
	<S extends T> Flux<S> batchInsert(@NonNull Publisher<S> entityStream);

	/**
	 * 按照主键更新一个对象
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity 待更新的对象
	 * @param <S>    更新对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> update(@NonNull S entity);

	/**
	 * 更新一个对象，只更新非空属性
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity 待更新的对象
	 * @param <S>    对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> updateSelective(@NonNull S entity);

	/**
	 * 更新一个对象，只更新指定的属性
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity  对象
	 * @param columns 待更新的属性，如果为空，则不更新
	 * @param <S>     对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> updateWith(@NonNull S entity, @Nullable Collection<SFunction<T, ?>> columns);

	/**
	 * 更新一个对象，忽略指定的属性
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity  对象
	 * @param columns 待更新的对象属性，如果为空，则更新所有属性 等价于 #update(entity)
	 * @param <S>     对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> updateWithout(@NonNull S entity, @Nullable Collection<SFunction<T, ?>> columns);

	/**
	 * 按照主键批量更新对象
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entities 对象集合
	 * @param <S>      对象的类型
	 * @return 更新后的对象
	 */
	<S extends T> Flux<S> batchUpdate(@NonNull Iterable<S> entities);

	/**
	 * 按照主键批量更新对象
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entityStream 对接集合的流
	 * @param <S>          对象的类型
	 * @return 更新后的对象
	 */
	<S extends T> Flux<S> batchUpdate(@NonNull Publisher<S> entityStream);

	/**
	 * 根据主键查询对象
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param id 主键
	 * @return 对象
	 */
	Mono<T> get(@NonNull ID id);

	/**
	 * 根据主键查询对象
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param idPublisher 主键的流
	 * @return 对象
	 */
	Mono<T> get(@NonNull Publisher<ID> idPublisher);

	/**
	 * 判断对象是否存在
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param id 主键
	 * @return 是否存在 true 存在，false 不存在
	 */
	Mono<Boolean> existsById(@NonNull ID id);

	/**
	 * 判断对象是否存在
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param idPublisher 主键的流
	 * @return 是否存在 true 存在，false 不存在
	 */
	Mono<Boolean> existsById(@NonNull Publisher<ID> idPublisher);

	/**
	 * 按照主键删除对象
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 * 如果对象中有  {@link IsDeleted} 则是逻辑删除
	 *
	 * @param id 主键
	 * @return 影响的行数
	 */
	Mono<Integer> delete(@NonNull ID id);

	/**
	 * 按照主键删除对象
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 * 如果对象中有  {@link IsDeleted} 则是逻辑删除
	 *
	 * @param idPublisher 主键的流
	 * @return 影响的行数
	 */
	Mono<Integer> delete(@NonNull Publisher<ID> idPublisher);

	/**
	 * 按照动态条件删除对象
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 * 如果对象中有  {@link IsDeleted} 则是逻辑删除
	 *
	 * @param criteria 动态条件
	 * @return 影响的行数
	 */
	Mono<Integer> deleteByCriteria(@NonNull Criteria<T> criteria);

	/**
	 * 按照动态条件查询对象(返回多个结果)
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param criteria 动态条件
	 * @return 查询的对象的集合
	 */
	Flux<T> find(@NonNull Criteria<T> criteria);

	/**
	 * 按照动态条件查询对象(返回一个结果)
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param criteria 动态条件
	 * @return 查询的对象
	 */
	Mono<T> findOne(@NonNull Criteria<T> criteria);

	/**
	 * 按照动态条件查询记录数
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param criteria 动态条件
	 * @return 记录数
	 */
	Mono<Long> count(@NonNull Criteria<T> criteria);

	/**
	 * 按照动态条件查询记录，并且分页
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param criteria    动态条件
	 * @param pageRequest 分页参数
	 * @return 分页查询结果
	 */
	Mono<Pagination<T>> findPage(@NonNull Criteria<T> criteria, @NonNull PageRequest pageRequest);

	/**
	 * 执行一个自定义的查询语句
	 *
	 * @param sql    查询语句
	 * @param params sql 查询参数
	 * @return 查询结果
	 */
	Flux<T> select(@NonNull String sql, @NonNull Map<String, R2dbcParam> params);
}
