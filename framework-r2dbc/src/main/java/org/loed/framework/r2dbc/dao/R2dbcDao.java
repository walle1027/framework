package org.loed.framework.r2dbc.dao;

import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.po.IsDeleted;
import org.loed.framework.common.po.TenantId;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.PageRequest;
import org.loed.framework.common.query.Pagination;
import org.loed.framework.r2dbc.query.R2dbcParam;
import org.reactivestreams.Publisher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Predicate;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/6 7:42 下午
 */
@SuppressWarnings("unchecked")
public interface R2dbcDao<T, ID> {
	/**
	 * 新增一个对象
	 * 自动集成{@link javax.persistence.Column#insertable() 的设置}
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
	 * 自动集成{@link javax.persistence.Column#insertable() 的设置}
	 *
	 * @param entityStream 对象流
	 * @param <S>          对象的子类
	 * @return 新增后的对象
	 */
	<S extends T> Flux<S> batchInsert(@NonNull Publisher<S> entityStream);

	/**
	 * 按照主键更新一个对象
	 * 自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity 待更新的对象
	 * @param <S>    更新对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> update(@NonNull S entity);

	/**
	 * 更新一个对象，按照列判断器判断需要更新哪些属性
	 * 自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity    待更新的对象
	 * @param predicate 属性判断器，如果判断器返回true，则更新此对象，否则不更新，如果没有一个列需要更新，则抛出异常
	 * @param <S>       对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> update(@NonNull S entity, Predicate<Column> predicate);

	/**
	 * 更新一个对象，只更新指定的属性
	 * 自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity  对象
	 * @param columns 待更新的属性，如果为空，则不更新任何属性
	 * @param <S>     对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S>  updateWith(@NonNull S entity, @Nullable SFunction<T, ?>... columns);

	/**
	 * 更新一个对象，忽略指定的属性
	 * 自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity  对象
	 * @param columns 待更新的对象属性，如果为空，则更新所有属性 等价于 {@link R2dbcDao#update(java.lang.Object)}
	 * @param <S>     对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> updateWithout(@NonNull S entity, @Nullable SFunction<T, ?>... columns);

	/**
	 * 按照主键批量更新对象
	 * 自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entityStream 对接集合的流
	 * @param <S>          对象的类型
	 * @return 更新后的对象
	 */
	<S extends T> Flux<S> batchUpdate(@NonNull Publisher<S> entityStream);

	/**
	 * 按照主键批量更新对象，会自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entities 对象集合
	 * @param <S>      对象的类型
	 * @return 更新后的对象
	 */
	<S extends T> Flux<S> batchUpdate(@NonNull Iterable<S> entities);

	/**
	 * 按照主键批量更新对象,只更新指定的属性
	 * 自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entities 对象集合
	 * @param columns  待更新的属性,如果为空，则不更新任何属性
	 * @param <S>      对象的类型
	 * @return 更新后的对象
	 */
	<S extends T> Flux<S> batchUpdateWith(@NonNull Iterable<S> entities, @Nullable SFunction<T, ?>... columns);

	/**
	 * 按照主键批量更新对象,忽略指定的属性，会自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entities 对象集合
	 * @param columns  忽略更新的对象属性，如果为空，则更新所有属性 等价于 {@link R2dbcDao#batchUpdate(java.lang.Iterable)}
	 * @param <S>      对象的类型
	 * @return 更新后的对象
	 */
	<S extends T> Flux<S> batchUpdateWithout(@NonNull Iterable<S> entities, @Nullable SFunction<T, ?>... columns);


	/**
	 * 更新一个对象，只更新非空属性
	 * 自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity 待更新的对象
	 * @param <S>    对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> updateNonNull(@NonNull S entity);

	/**
	 * 更新一个对象，只更新非空属性，如果指定了其他属性，同时更新其他属性(无论是否为空)
	 * 自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entity  对象
	 * @param columns 待更新的属性，如果为空，只更新非空属性
	 * @param <S>     对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> updateNonNullAnd(@NonNull S entity, @Nullable SFunction<T, ?>... columns);

	/**
	 * 按照主键批量更新对象,只更新非空属性
	 * 自动集成{@link javax.persistence.Column#updatable() 的配置}
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entities 对象集合
	 * @param <S>      对象的类型
	 * @return 更新后的对象
	 */
	<S extends T> Flux<S> batchUpdateNonNull(@NonNull Iterable<S> entities);

	/**
	 * 按照主键批量更新对象,忽略指定的属性
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param entities 对象集合
	 * @param columns  忽略更新的对象属性，如果为空，则更新所有属性 等价于 {@link R2dbcDao#batchUpdate(java.lang.Iterable)}
	 * @param <S>      对象的类型
	 * @return 更新后的对象
	 */
	<S extends T> Flux<S> batchUpdateNonNullAnd(@NonNull Iterable<S> entities, @Nullable SFunction<T, ?>... columns);

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
	Mono<Integer> delete(@NonNull Criteria<T> criteria);

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
	 * 按照属性名=属性值查询对象(返回多个结果)
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param property 属性名
	 * @param value    属性值
	 * @return 查询的对象的集合
	 */
	<R> Flux<T> findByProperty(@NonNull SFunction<T, R> property, R value);

	/**
	 * 检查对象的某个属性是否重复，如果传入主键，将忽略主键对应的记录
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param id       主键，可以为null，如果为null
	 * @param property 属性名
	 * @param value    属性值
	 * @return 查询的对象的集合
	 */
	<R> Mono<Boolean> isRepeated(@Nullable ID id, @NonNull SFunction<T, R> property, @NonNull R value);

	/**
	 * 按照动态条件查询记录，并且分页
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param criteria 动态条件
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

	/**
	 * 执行一个自定义的dml语句
	 *
	 * @param sql    查询语句
	 * @param params sql 查询参数
	 * @return 影响行数
	 */
	Mono<Integer> execute(@NonNull String sql, @Nullable Map<String, R2dbcParam> params);
}
