package org.loed.framework.r2dbc.dao;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.database.Column;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.r2dbc.listener.OrderedListener;
import org.loed.framework.r2dbc.listener.spi.*;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.persistence.GenerationType;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/7 3:20 下午
 */
@SuppressWarnings("unchecked")
@Slf4j
public class DefaultR2DbcDao<T, ID> implements R2dbcDao<T, ID> {
	private final DatabaseClient databaseClient;

	private final Class<? extends R2dbcDao<T, ID>> daoInterface;

	private final Class<T> entityClass;

	private final Table table;

	private final Class<ID> idClass;

	private final JPAClassRowMapper<T> mapper;

	private int batchSize = 200;

	private R2dbcSqlBuilder sqlBuilder;

	private List<PreInsertListener> preInsertListeners;

	private List<PostInsertListener> postInsertListeners;

	private List<PreUpdateListener> preUpdateListeners;

	private List<PostUpdateListener> postUpdateListeners;

	private List<PreDeleteListener> preDeleteListeners;

	private List<PostDeleteListener> postDeleteListeners;

	public DefaultR2DbcDao(Class<? extends R2dbcDao<T, ID>> daoInterface, DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
		this.daoInterface = daoInterface;
		this.entityClass = (Class<T>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[0];
		this.idClass = (Class<ID>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[1];
		this.mapper = new JPAClassRowMapper<>(entityClass);
		this.table = ORMapping.get(entityClass);
		if (this.table == null) {
			throw new RuntimeException("error class ");
		}
	}

	@Override
	public <S extends T> Mono<S> insert(S entity) {
		return Mono.just(entity).flatMap(po -> {
			if (preInsertListeners != null) {
				return Flux.fromIterable(preInsertListeners).sort(Comparator.comparingInt(OrderedListener::getOrder))
						.flatMap(preInsertListener -> {
							return preInsertListener.preInsert(po).doOnError(err -> {
								log.error(err.getMessage(), err);
							}).onErrorStop();
						}).last();
			} else {
				return Mono.just(po);
			}
		}).flatMap(this::doInsert).flatMap(po -> {
			if (postInsertListeners != null) {
				return Flux.fromIterable(postInsertListeners).sort(Comparator.comparing(OrderedListener::getOrder)).flatMap(postInsertListener -> {
					return postInsertListener.postInsert(po);
				}).last();
			} else {
				return Mono.just(po);
			}
		});
	}

	protected <S extends T> Mono<S> doInsert(S entity) {
		Optional<Column> idColumn = table.getColumns().stream().filter(Column::isPk).findFirst();
		if (!idColumn.isPresent()) {
			return Mono.error(new NoSuchFieldError("id property"));
		}
		Tuple2<String, List<Tuple2<String, Class<?>>>> insert = sqlBuilder.insert(table);
		String sql = insert.getT1();
		List<Tuple2<String, Class<?>>> params = insert.getT2();
		DatabaseClient.GenericExecuteSpec execute = databaseClient.execute(sql);
		if (params.size() > 0) {
			for (Tuple2<String, Class<?>> param : params) {
				Object fieldValue = ReflectionUtils.getFieldValue(entity, param.getT1());
				if (fieldValue == null) {
					execute = execute.bindNull(param.getT1(), param.getT2());
				} else {
					execute = execute.bind(param.getT1(), fieldValue);
				}
			}
		}
		GenerationType idGenerationType = table.getIdGenerationType();
		if (idGenerationType.equals(GenerationType.AUTO)) {
			return execute.map(row -> {
				//TODO check the auto increase Id
				return (ID) row.get("insertedId");
			}).one().map(id -> {
				//set the id
				ReflectionUtils.setFieldValue(entity, idColumn.get().getJavaName(), id);
				return entity;
			});
		} else {
			return execute.fetch().rowsUpdated().thenReturn(entity);
		}
	}

	@Override
	public <S extends T> Flux<S> batchInsert(Iterable<S> entities) {
		return Flux.fromIterable(entities).flatMap(preInsertFunction()).collectList().flatMapMany(batchInsertFunction()).flatMap(postInsertFunction());
	}




	@Override
	public <S extends T> Flux<S> batchInsert(Publisher<S> entityStream) {
		return Flux.from(entityStream).flatMap(preInsertFunction()).collectList().flatMapMany(batchInsertFunction()).flatMap(postInsertFunction());
	}

	private <S extends T> Function<List<S>, Publisher<? extends S>> batchInsertFunction() {
		return entityList -> {
			int size = entityList.size();
			int n = size / batchSize;
			int mod = size % batchSize;
			if (mod != 0) {
				n++;
			}
			Flux<S> flux = Flux.empty();
			for (int i = 0; i < n; i++) {
				int endIndex = Math.min((i + 1) * batchSize, entityList.size());
				List<S> subList = entityList.subList(i * batchSize, endIndex);
				flux = Flux.concat(flux, doBatchInsert(subList));
			}
			return flux;
		};
	}

	protected <S extends T> Flux<S> doBatchInsert(List<S> entityList) {
		int batchSize = entityList.size();
		Tuple2<String, List<Tuple2<String, Class<?>>>> batchInsert = sqlBuilder.batchInsert(table, batchSize);
		List<Tuple2<String, Class<?>>> params = batchInsert.getT2();
		DatabaseClient.GenericExecuteSpec execute = databaseClient.execute(batchInsert.getT1());
		if (params.size() > 0) {
			for (Tuple2<String, Class<?>> param : params) {
				String paramName = param.getT1();
				Class<?> paramClass = param.getT2();
				for (int i = 0; i < batchSize; i++) {
					S po = entityList.get(i);
					Object paramValue = ReflectionUtils.getFieldValue(po, paramName);
					if (paramValue == null) {
						execute = execute.bindNull(paramName + i, paramClass);
					} else {
						execute = execute.bind(paramName + i, paramValue);
					}
				}
			}
		}
		return execute.fetch().rowsUpdated().doOnError(err -> {
			log.error(err.getMessage(), err);
		}).switchIfEmpty(Mono.fromSupplier(() -> {
			return -1;
		})).flatMapMany(rows -> {
			log.debug("rows updated :" + rows);
			return Flux.fromIterable(entityList);
		});
	}

	private <S extends T> Function<S, Publisher<? extends S>> preInsertFunction() {
		return entity -> {
			if (preInsertListeners != null) {
				return Flux.fromIterable(preInsertListeners).sort(Comparator.comparing(OrderedListener::getOrder)).flatMap(preInsertListener -> {
					return preInsertListener.preInsert(entity).onErrorStop().doOnError(err -> {
						log.error(err.getMessage(), err);
					});
				}).last();
			} else {
				return Mono.just(entity);
			}
		};
	}

	private <S extends T> Function<S, Publisher<? extends S>> postInsertFunction() {
		return entity -> {
			if (postInsertListeners != null) {
				return Flux.fromIterable(postInsertListeners).sort(Comparator.comparing(OrderedListener::getOrder)).flatMap(postInsertListener -> {
					return postInsertListener.postInsert(entity).onErrorStop().doOnError(err -> {
						log.error(err.getMessage(), err);
					});
				});
			} else {
				return Flux.just(entity);
			}
		};
	}

	@Override
	public <S extends T> Mono<S> update(S entity) {
		return Mono.just(entity).flatMap(e -> {
			if (preUpdateListeners != null) {
				return Flux.fromIterable(preUpdateListeners).flatMap(preUpdateListener -> {
					return preUpdateListener.preUpdate(e).onErrorStop().doOnError(err -> {
						log.error(err.getMessage(), err);
					}).thenReturn(e);
				}).last();
			}
			return Mono.just(e);
		}).flatMap(this::doUpdate).flatMap(e -> {
			if (postUpdateListeners != null) {
				return Flux.fromIterable(postUpdateListeners).flatMap(postUpdateListener -> {
					return postUpdateListener.postUpdate(e).onErrorStop().doOnError(err -> {
						log.error(err.getMessage(), err);
					}).thenReturn(e);
				}).last();
			} else {
				return Mono.just(e);
			}
		});
	}

	private <S> Mono<S> doUpdate(S entity) {
		Optional<Column> idColumn = table.getColumns().stream().filter(Column::isPk).findFirst();
		if (!idColumn.isPresent()) {
			return Mono.error(new RuntimeException("no id column"));
		}
		ID id = (ID) ReflectionUtils.getFieldValue(entity, idColumn.get().getJavaName());
		if (id == null) {
			return Mono.error(new RuntimeException("id is null "));
		}
		Tuple2<String, List<Tuple2<String, Class<?>>>> update = sqlBuilder.update(table);
		String sql = update.getT1();
		List<Tuple2<String, Class<?>>> params = update.getT2();
		DatabaseClient.GenericExecuteSpec execute = databaseClient.execute(sql);
		if (params.size() > 0) {
			for (Tuple2<String, Class<?>> param : params) {
				String paramName = param.getT1();
				Class<?> paramClass = param.getT2();
				Object fieldValue = ReflectionUtils.getFieldValue(entity, paramName);
				if (fieldValue == null) {
					execute = execute.bindNull(paramName, paramClass);
				} else {
					execute = execute.bind(paramName, fieldValue);
				}
			}
		}
		return execute.fetch().rowsUpdated().map(rowsUpdated -> {
			return entity;
		});
	}

	@Override
	public <S extends T> Mono<S> updateWith(S entity, Collection<SFunction<T, ?>> columns) {
		return null;
	}

	@Override
	public <S extends T> Mono<S> updateWithout(S entity, SFunction<T, ?>... columns) {
		return null;
	}

	@Override
	public <S extends T> Flux<S> batchUpdate(Iterable<S> entities) {
		return null;
	}

	@Override
	public <S extends T> Flux<S> batchUpdate(Publisher<S> entityStream) {
		return null;
	}

	@Override
	public Mono<T> get(ID id) {
		return null;
	}

	@Override
	public Mono<T> get(Publisher<ID> id) {
		return null;
	}

	@Override
	public Mono<Boolean> existsById(ID id) {
		return null;
	}

	@Override
	public Mono<Boolean> existsById(Publisher<ID> id) {
		return null;
	}

	@Override
	public Mono<Void> delete(ID id) {
		return null;
	}

	@Override
	public Mono<Void> delete(Publisher<ID> id) {
		return null;
	}

	@Override
	public Mono<Void> deleteByCriteria(Criteria<T> criteria) {
		return null;
	}

	@Override
	public Flux<T> find(Criteria<T> criteria) {
		return null;
	}

	@Override
	public Mono<T> findOne(Criteria<T> criteria) {
		return null;
	}

	@Override
	public Mono<Long> count(Criteria<T> criteria) {
		return null;
	}

	@Override
	public Flux<T> findPage(Criteria<T> criteria, PageRequest pageRequest) {
		return null;
	}

	@Override
	public Flux<T> select(String sql) {
		return null;
	}


	public void setPreInsertListeners(List<PreInsertListener> preInsertListeners) {
		this.preInsertListeners = preInsertListeners;
	}

	public void setSqlBuilder(R2dbcSqlBuilder sqlBuilder) {
		this.sqlBuilder = sqlBuilder;
	}

	public void setPostInsertListeners(List<PostInsertListener> postInsertListeners) {
		this.postInsertListeners = postInsertListeners;
	}

	public void setPreUpdateListeners(List<PreUpdateListener> preUpdateListeners) {
		this.preUpdateListeners = preUpdateListeners;
	}

	public void setPostUpdateListeners(List<PostUpdateListener> postUpdateListeners) {
		this.postUpdateListeners = postUpdateListeners;
	}

	public void setPreDeleteListeners(List<PreDeleteListener> preDeleteListeners) {
		this.preDeleteListeners = preDeleteListeners;
	}

	public void setPostDeleteListeners(List<PostDeleteListener> postDeleteListeners) {
		this.postDeleteListeners = postDeleteListeners;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
