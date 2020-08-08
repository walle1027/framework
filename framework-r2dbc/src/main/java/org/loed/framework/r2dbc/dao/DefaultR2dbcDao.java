package org.loed.framework.r2dbc.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.database.Column;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.po.IsDeleted;
import org.loed.framework.common.po.TenantId;
import org.loed.framework.common.query.Condition;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.Operator;
import org.loed.framework.common.query.Pagination;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.r2dbc.listener.OrderedListener;
import org.loed.framework.r2dbc.listener.spi.*;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.GenerationType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/7 3:20 下午
 */
@SuppressWarnings("unchecked")
@Slf4j
public class DefaultR2dbcDao<T, ID> implements R2dbcDao<T, ID> {
	private final DatabaseClient databaseClient;

	private final Class<? extends R2dbcDao<T, ID>> daoInterface;

	private final Class<T> entityClass;

	private final Table table;

	private final Column idColumn;

	private final Class<ID> idClass;

	private final JPAClassRowMapper<T> mapper;

	private int batchSize = 200;

	private R2dbcSqlBuilder r2dbcSqlBuilder;

	private List<PreInsertListener> preInsertListeners;

	private List<PostInsertListener> postInsertListeners;

	private List<PreUpdateListener> preUpdateListeners;

	private List<PostUpdateListener> postUpdateListeners;

	private List<PreDeleteListener> preDeleteListeners;

	private List<PostDeleteListener> postDeleteListeners;

	public DefaultR2dbcDao(Class<? extends R2dbcDao<T, ID>> daoInterface, DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
		this.daoInterface = daoInterface;
		this.entityClass = (Class<T>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[0];
		this.idClass = (Class<ID>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[1];
		this.mapper = new JPAClassRowMapper<>(entityClass);
		this.table = ORMapping.get(entityClass);
		if (this.table == null) {
			throw new RuntimeException("error class ");
		}
		this.idColumn = table.getColumns().stream().filter(Column::isPk).findFirst().orElseThrow(() -> {
			return new RuntimeException("id column is not exits");
		});
	}

	@Override
	public <S extends T> Mono<S> insert(@NonNull S entity) {
		return Mono.just(entity).flatMap(preInsertFunction()).flatMap(this::doInsert).flatMap(postInsertFunction());
	}

	protected <S extends T> Mono<S> doInsert(S entity) {
		R2dbcQuery query = r2dbcSqlBuilder.insert(entity, table);
		DatabaseClient.GenericExecuteSpec execute = bind(query);
		GenerationType idGenerationType = table.getIdGenerationType();
		if (idGenerationType.equals(GenerationType.AUTO)) {
			return execute.map(row -> {
				//TODO check the auto increase Id
				return (ID) row.get("insertedId");
			}).one().map(id -> {
				//set the id
				ReflectionUtils.setFieldValue(entity, idColumn.getJavaName(), id);
				return entity;
			});
		} else {
			return execute.fetch().rowsUpdated().thenReturn(entity);
		}
	}

	@Override
	public <S extends T> Flux<S> batchInsert(@NonNull Iterable<S> entities) {
		return Flux.fromIterable(entities).flatMap(preInsertFunction()).collectList().flatMapMany(batchInsertFunction()).flatMap(postInsertFunction());
	}


	@Override
	public <S extends T> Flux<S> batchInsert(@NonNull Publisher<S> entityStream) {
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
		R2dbcQuery query = r2dbcSqlBuilder.batchInsert(entityList, table);
		DatabaseClient.GenericExecuteSpec execute = bind(query);
		return execute.fetch().rowsUpdated().doOnError(err -> {
			log.error(err.getMessage(), err);
		}).switchIfEmpty(Mono.fromSupplier(() -> {
			return -1;
		})).flatMapMany(rows -> {
			log.debug("rows updated :" + rows);
			return Flux.fromIterable(entityList);
		});
	}

	private <S extends T> Function<S, Mono<? extends S>> preInsertFunction() {
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

	private <S extends T> Function<S, Mono<? extends S>> postInsertFunction() {
		return entity -> {
			if (postInsertListeners != null) {
				return Flux.fromIterable(postInsertListeners).sort(Comparator.comparing(OrderedListener::getOrder)).flatMap(postInsertListener -> {
					return postInsertListener.postInsert(entity).onErrorStop().doOnError(err -> {
						log.error(err.getMessage(), err);
					});
				}).last();
			} else {
				return Mono.just(entity);
			}
		};
	}

	@Override
	public <S extends T> Mono<S> update(@NonNull S entity) {
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, R2dbcSqlBuilder.ALWAYS_TRUE_FILTER)).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Mono<S> updateSelective(@NonNull S entity) {
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, new R2dbcSqlBuilder.NotEmptyFilter(po))).flatMap(postUpdateFunction());
	}

	private <S extends T> Function<S, Mono<? extends S>> preUpdateFunction() {
		return entity -> {
			if (preUpdateListeners != null) {
				return Flux.fromIterable(preUpdateListeners).sort(Comparator.comparing(OrderedListener::getOrder)).flatMap(preUpdateListener -> {
					return preUpdateListener.preUpdate(entity).onErrorStop().doOnError(err -> {
						log.error(err.getMessage(), err);
					});
				}).last();
			} else {
				return Mono.just(entity);
			}
		};
	}

	private <S extends T> Function<S, Mono<? extends S>> postUpdateFunction() {
		return entity -> {
			if (postUpdateListeners != null) {
				return Flux.fromIterable(postUpdateListeners).sort(Comparator.comparing(OrderedListener::getOrder)).flatMap(postUpdateListener -> {
					return postUpdateListener.postUpdate(entity).onErrorStop().doOnError(err -> {
						log.error(err.getMessage(), err);
					});
				}).last();
			} else {
				return Mono.just(entity);
			}
		};
	}

	private <S> Mono<S> doUpdate(S entity, Predicate<Column> columnFilter) {
		ID id = (ID) ReflectionUtils.getFieldValue(entity, idColumn.getJavaName());
		if (id == null) {
			return Mono.error(new RuntimeException("id is null "));
		}
		return Flux.merge(Flux.just(new Condition(idColumn.getJavaName(), Operator.equal, id)), addConditions())
				.collectList().map(conditions -> {
					return r2dbcSqlBuilder.updateByCriteria(entity, table, Criteria.from(entityClass).criterion(conditions.toArray(new Condition[0])), columnFilter);
				}).flatMap(r2dbcQuery -> {
					return execute(r2dbcQuery).thenReturn(entity);
				});
	}

	@Override
	public <S extends T> Mono<S> updateWith(@NonNull S entity, @Nullable Collection<SFunction<T, ?>> columns) {
		List<String> includes = columns == null ? null : columns.stream().map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, includes == null ? R2dbcSqlBuilder.ALWAYS_TRUE_FILTER : new R2dbcSqlBuilder.IncludeFilter(includes))).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Mono<S> updateWithout(@NonNull S entity, @Nullable Collection<SFunction<T, ?>> columns) {
		List<String> excludes = columns == null ? null : columns.stream().map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, excludes == null ? R2dbcSqlBuilder.ALWAYS_TRUE_FILTER : new R2dbcSqlBuilder.ExcludeFilter(excludes))).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Flux<S> batchUpdate(@NonNull Iterable<S> entities) {
		return Flux.fromIterable(entities).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, R2dbcSqlBuilder.ALWAYS_TRUE_FILTER)).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Flux<S> batchUpdate(@NonNull Publisher<S> entityStream) {
		return Flux.from(entityStream).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, R2dbcSqlBuilder.ALWAYS_TRUE_FILTER)).flatMap(postUpdateFunction());
	}

	@Override
	public Mono<T> get(@NonNull ID id) {
		return Flux.merge(Flux.just(new Condition(idColumn.getJavaName(), Operator.equal, id)), addConditions()).collectList().map(conditions -> {
			return Criteria.from(entityClass).criterion(conditions.toArray(new Condition[0]));
		}).flatMap(this::findOne);
	}

	@Override
	public Mono<T> get(@NonNull Publisher<ID> idPublisher) {
		return Flux.merge(Flux.from(idPublisher).map(id -> {
			return new Condition(idColumn.getJavaName(), Operator.equal, id);
		}), addConditions()).collectList().map(conditions -> {
			return Criteria.from(entityClass).criterion(conditions.toArray(new Condition[0]));
		}).flatMap(this::findOne);
	}

	private Flux<Condition> addConditions() {
		List<Field> fields = ReflectionUtils.getDeclaredFields(entityClass);
		Flux<Condition> flux = Flux.empty();
		for (Field field : fields) {
			if (field.getAnnotation(TenantId.class) != null) {
				flux = flux.mergeWith(ReactiveSystemContext.getTenantCode().map(tenantCode -> {
					return new Condition(field.getName(), Operator.equal, tenantCode);
				}));
			}
			if (field.getAnnotation(IsDeleted.class) != null) {
				flux = flux.mergeWith(Mono.just(new Condition(field.getName(), Operator.equal, (byte) 0)));
			}
		}
		return flux;
	}

	@Override
	public Mono<Boolean> existsById(@NonNull ID id) {
		return Flux.merge(Flux.just(new Condition(idColumn.getJavaName(), Operator.equal, id)), addConditions()).collectList().map(conditions -> {
			return Criteria.from(entityClass).criterion(conditions.toArray(new Condition[0]));
		}).flatMap(this::count).map(count -> count > 0);
	}

	@Override
	public Mono<Boolean> existsById(@NonNull Publisher<ID> idPublisher) {
		return Flux.from(idPublisher).map(id -> {
			return new Condition(idColumn.getJavaName(), Operator.equal, id);
		}).mergeWith(addConditions())
				.collectList().map(conditions -> {
					return Criteria.from(entityClass).criterion(conditions.toArray(new Condition[0]));
				}).flatMap(this::count).map(count -> count > 0);
	}

	@Override
	public Mono<Integer> delete(@NonNull ID id) {
		return Flux.merge(Flux.just(new Condition(idColumn.getJavaName(), Operator.equal, id)), addConditions()).collectList()
				.map(conditions -> {
					return Criteria.from(entityClass).criterion(conditions.toArray(new Condition[0]));
				}).flatMap(this::deleteByCriteria);
	}

	@Override
	public Mono<Integer> delete(@NonNull Publisher<ID> idPublisher) {
		return Flux.merge(Flux.from(idPublisher).map(id -> {
			return new Condition(idColumn.getJavaName(), Operator.equal, id);
		}), addConditions())
				.collectList()
				.map(conditions -> Criteria.from(entityClass).criterion(conditions.toArray(new Condition[0])))
				.flatMap(this::deleteByCriteria);
	}

	@Override
	public Mono<Integer> deleteByCriteria(@NonNull Criteria<T> criteria) {
		return Mono.just(criteria).flatMap(crit -> {
					return addConditions().collectList().map(conditions -> {
						return crit.criterion(conditions.toArray(new Condition[0]));
					}).defaultIfEmpty(crit);
				}
		).map(crit -> {
			if (CollectionUtils.isEmpty(crit.getConditions())) {
				throw new RuntimeException("empty conditions to delete the entity ,this will ignore");
			}
			return r2dbcSqlBuilder.deleteByCriteria(table, crit);
		}).flatMap(this::execute);
	}

	@Override
	public Flux<T> find(@NonNull Criteria<T> criteria) {
		List<Condition> conditions = criteria.getConditions() == null ? Collections.emptyList() : criteria.getConditions();
		return Flux.merge(Flux.fromIterable(conditions), addConditions()).collectList().map(cnd -> {
			Criteria<T> criteriaNew = Criteria.from(criteria);
			criteriaNew.setConditions(cnd);
			return criteriaNew;
		}).defaultIfEmpty(criteria).map(crt -> {
			return r2dbcSqlBuilder.findByCriteria(table, crt);
		}).flatMapMany(this::query);
	}

	@Override
	public Mono<T> findOne(@NonNull Criteria<T> criteria) {
		PageRequest request = PageRequest.of(1, 1);
		return findPage(criteria, request).flatMap(pagination -> {
			return pagination.getRows() == null ? Mono.empty() : Mono.just(pagination.getRows().get(0));
		});
	}

	@Override
	public Mono<Long> count(@NonNull Criteria<T> criteria) {
		List<Condition> conditions = criteria.getConditions() == null ? Collections.emptyList() : criteria.getConditions();
		return Flux.merge(Flux.fromIterable(conditions), addConditions()).collectList().map(cnds -> {
			Criteria<T> criteriaNew = Criteria.from(criteria);
			criteriaNew.setConditions(cnds);
			return criteriaNew;
		}).defaultIfEmpty(criteria)
				.flatMap(crit -> {
					R2dbcQuery query = r2dbcSqlBuilder.countByCriteria(table, crit);
					DatabaseClient.GenericExecuteSpec exec = bind(query);
					return exec.as(Long.class).fetch().one();
				});
	}

	@Override
	public Mono<Pagination<T>> findPage(@NonNull Criteria<T> criteria, @NonNull PageRequest pageRequest) {
		boolean paged = pageRequest.isPaged();
		if (paged) {
			return Mono.zip(count(criteria), Mono.just(r2dbcSqlBuilder.findPageByCriteria(table, criteria, pageRequest)).flatMap(r2dbcQuery -> {
				return query(r2dbcQuery).collectList();
			})).map(tup -> {
				Pagination<T> pagination = new Pagination<>();
				pagination.setPageNo(pageRequest.getPageNumber());
				pagination.setPageSize(pageRequest.getPageSize());
				pagination.setTotal(tup.getT1());
				pagination.setRows(tup.getT2());
				return pagination;
			});
		} else {
			return find(criteria).collectList().map(result -> {
				Pagination<T> pagination = new Pagination<>();
				pagination.setPageNo(pageRequest.getPageNumber());
				pagination.setPageSize(pageRequest.getPageSize());
				pagination.setTotal(result.size());
				pagination.setRows(result);
				return pagination;
			});
		}
	}

	@Override
	public Flux<T> select(@NonNull String sql, @NonNull Map<String, Object> params) {
		DatabaseClient.GenericExecuteSpec execute = databaseClient.execute(sql);
		if (params != null && params.size() > 0) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				execute = execute.bind(key, value);
			}
		}

		return null;
	}

	private Mono<Integer> execute(R2dbcQuery query) {
		DatabaseClient.GenericExecuteSpec bind = bind(query);
		return bind.fetch().rowsUpdated();
	}

	private DatabaseClient.GenericExecuteSpec bind(R2dbcQuery query) {
		DatabaseClient.GenericExecuteSpec execute = databaseClient.execute(query.getStatement());
		Map<String, R2dbcParam> params = query.getParams();
		if (params != null) {
			for (Map.Entry<String, R2dbcParam> entry : params.entrySet()) {
				String paramName = entry.getKey();
				R2dbcParam param = entry.getValue();
				if (param.getParamValue() == null) {
					execute = execute.bindNull(paramName, param.getParamType());
				} else {
					execute = execute.bind(paramName, param.getParamValue());
				}
			}
		}
		return execute;
	}

	private Flux<T> query(R2dbcQuery query) {
		DatabaseClient.GenericExecuteSpec bind = bind(query);
		return bind.map(mapper).all();
	}

	public void setPreInsertListeners(List<PreInsertListener> preInsertListeners) {
		this.preInsertListeners = preInsertListeners;
	}

	public void setR2dbcSqlBuilder(R2dbcSqlBuilder r2dbcSqlBuilder) {
		this.r2dbcSqlBuilder = r2dbcSqlBuilder;
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
