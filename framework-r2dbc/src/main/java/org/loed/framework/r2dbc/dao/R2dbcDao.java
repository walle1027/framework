package org.loed.framework.r2dbc.dao;

import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.query.Criteria;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/6 7:42 下午
 */
public interface R2dbcDao<T, ID> {
	<S extends T> Mono<S> insert(S entity);

	<S extends T> Flux<S> batchInsert(Iterable<S> entities);

	<S extends T> Flux<S> batchInsert(Publisher<S> entityStream);

	<S extends T> Mono<S> update(S entity);

	/**
	 * 更新一个对象，只更新指定的属性
	 *
	 * @param entity  对象
	 * @param columns 待更新的属性，如果为空，则不更新
	 * @param <S>     对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> updateWith(S entity, Collection<SFunction<T, ?>> columns);

	/**
	 * 更新一个对象，忽略指定的属性
	 *
	 * @param entity  对象
	 * @param columns 待更新的对象属性，如果为空，则更新所有属性 等价于 #update(entity)
	 * @param <S>     对象的子类
	 * @return 更新后的对象
	 */
	<S extends T> Mono<S> updateWithout(S entity, SFunction<T, ?>... columns);

	<S extends T> Flux<S> batchUpdate(Iterable<S> entities);

	<S extends T> Flux<S> batchUpdate(Publisher<S> entityStream);

	Mono<T> get(ID id);

	Mono<T> get(Publisher<ID> id);

	Mono<Boolean> existsById(ID id);

	Mono<Boolean> existsById(Publisher<ID> id);

	Mono<Void> delete(ID id);

	Mono<Void> delete(Publisher<ID> id);

	Mono<Void> deleteByCriteria(Criteria<T> criteria);

	Flux<T> find(Criteria<T> criteria);

	Mono<T> findOne(Criteria<T> criteria);

	Mono<Long> count(Criteria<T> criteria);

	Flux<T> findPage(Criteria<T> criteria, PageRequest pageRequest);

	Flux<T> select(String sql);
}
