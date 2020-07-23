package org.loed.framework.r2dbc.dao;

import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.query.Criteria;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

	<S extends T> Mono<S> updateWith(S entity, SFunction<T, ?>... columns);

	<S extends T> Mono<S> updateWithout(S entity, SFunction<T, ?>... columns);

	<S extends T> Flux<S> batchUpdate(Iterable<S> entities);

	<S extends T> Flux<S> batchUpdate(Publisher<S> entityStream);

	Mono<T> get(ID id);

	Mono<T> get(Publisher<ID> id);

	Mono<Boolean> existsById(ID id);

	Mono<Boolean> existsById(Publisher<ID> id);

	Mono<Void> delete(ID id);

	Mono<Void> delete(Publisher<ID> id);

	Mono<Void> deleteByCriteria(Criteria criteria);

	Flux<T> find(Criteria criteria);

	Mono<T> findOne(Criteria criteria);

	Mono<Long> count(Criteria criteria);

	Flux<T> findPage(Criteria criteria, PageRequest pageRequest);

	Flux<T> select(String sql);
}
