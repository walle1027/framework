package org.loed.framework.r2dbc.dao;

import io.r2dbc.spi.Batch;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Filters;
import org.loed.framework.common.orm.ORMapping;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.po.IsDeleted;
import org.loed.framework.common.po.TenantId;
import org.loed.framework.common.query.*;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.r2dbc.R2dbcException;
import org.loed.framework.r2dbc.listener.OrderedListener;
import org.loed.framework.r2dbc.listener.spi.*;
import org.loed.framework.r2dbc.query.R2dbcParam;
import org.loed.framework.r2dbc.query.R2dbcQuery;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilder;
import org.reactivestreams.Publisher;
import org.springframework.lang.NonNull;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import javax.persistence.GenerationType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/7 3:20 下午
 */
@SuppressWarnings({"unchecked", "unused"})
@Slf4j
public class DefaultR2dbcDao<T, ID> implements R2dbcDao<T, ID> {
	private final DatabaseClient databaseClient;

	@SuppressWarnings("FieldCanBeLocal")
	private final Class<? extends R2dbcDao<T, ID>> daoInterface;

	private final Class<T> entityClass;

	private final Table table;

	private final Column idColumn;

	private final Class<ID> idClass;

	private final ConnectionFactory connectionFactory;

	private final JPAClassRowMapper<T> mapper;

	private int batchSize = 200;

	private R2dbcSqlBuilder r2dbcSqlBuilder;

	private List<PreInsertListener> preInsertListeners;

	private List<PostInsertListener> postInsertListeners;

	private List<PreUpdateListener> preUpdateListeners;

	private List<PostUpdateListener> postUpdateListeners;

	private List<PreDeleteListener> preDeleteListeners;

	public DefaultR2dbcDao(Class<? extends R2dbcDao<T, ID>> daoInterface, DatabaseClient databaseClient, ConnectionFactory connectionFactory) {
		this.databaseClient = databaseClient;
		this.daoInterface = daoInterface;
		this.entityClass = (Class<T>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[0];
		this.idClass = (Class<ID>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[1];
		this.connectionFactory = connectionFactory;
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
			return execute.map((r, m) -> {
				return (ID) r.get(0, idClass);
			}).all().doOnError(err -> {
				log.info(err.getMessage(), err);
			}).last().map(id -> {
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
		if (table.getIdGenerationType() == GenerationType.AUTO) {
			//hacked by mysql
			return execute.map((r, m) -> {
				return Tuples.of(r.get(0, idClass), r.get(1, Long.class));
			}).first().flatMapMany(tuple -> {
				ID id = tuple.getT1();
				long count = tuple.getT2();
				for (int i = 0; i < count; i++) {
					S entity = entityList.get(i);
					if (id instanceof Long) {
						ReflectionUtils.setFieldValue(entity, idColumn.getJavaName(), (Long) id + i);
					} else if (id instanceof BigInteger) {
						ReflectionUtils.setFieldValue(entity, idColumn.getJavaName(), ((BigInteger) id).add(BigInteger.valueOf(i)));
					} else if (id instanceof Integer) {
						ReflectionUtils.setFieldValue(entity, idColumn.getJavaName(), (Integer) id + i);
					}
				}
				return Flux.fromIterable(entityList);
			});
		} else {
			return execute.fetch().rowsUpdated().doOnError(err -> {
				log.error(err.getMessage(), err);
			}).switchIfEmpty(Mono.fromSupplier(() -> {
				return -1;
			})).flatMapMany(rows -> {
				log.debug("rows updated :" + rows);
				return Flux.fromIterable(entityList);
			});
		}

	}

	private <S extends T> Function<S, Mono<? extends S>> preInsertFunction() {
		return entity -> {
			if (preInsertListeners != null) {
				return Flux.fromIterable(preInsertListeners).sort(Comparator.comparing(OrderedListener::getOrder))
						.flatMap(preInsertListener -> {
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
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, Filters.ALWAYS_TRUE_FILTER)).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Mono<S> update(S entity, Predicate<Column> predicate) {
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, predicate)).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Mono<S> updateWith(S entity, SFunction<T, ?>... columns) {
		if (columns == null || columns.length == 0) {
			return Mono.error(new R2dbcException("non columns to update"));
		}
		List<String> includes = Arrays.stream(columns).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, new Filters.IncludeFilter(includes))).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Mono<S> updateWithout(S entity, SFunction<T, ?>... columns) {
		List<String> excludes = columns == null ? null : Arrays.stream(columns).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, excludes == null ? Filters.ALWAYS_TRUE_FILTER : new Filters.ExcludeFilter(excludes))).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Mono<S> updateNonNull(@NonNull S entity) {
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po, new Filters.NonNullFilter(po))).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Mono<S> updateNonNullAnd(S entity, SFunction<T, ?>... columns) {
		List<String> includes = (columns == null || columns.length == 0) ? null : Arrays.stream(columns).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		return Mono.just(entity).flatMap(preUpdateFunction()).flatMap(po -> doUpdate(po,
				includes == null ? new Filters.NonNullFilter(po) : new Filters.NonNullFilter(po).or(new Filters.IncludeFilter(includes)))
		).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Flux<S> batchUpdateNonNull(Iterable<S> entities) {
		return Flux.fromIterable(entities).flatMap(preUpdateFunction()).collectList().flatMapMany(entityList -> {
			return doBatchUpdateNonNull(entityList, null).thenMany(Flux.fromIterable(entityList));
		}).flatMap(postUpdateFunction());
	}

	private <S> Mono<Integer> doBatchUpdateNonNull(List<S> entityList, Predicate<Column> includes) {
		return Mono.zip(commonConditions().collectList(), ConnectionFactoryUtils.getConnection(connectionFactory)).flatMap(tup -> {
			List<Condition> conditions = tup.getT1();
			Connection connection = tup.getT2();
			return Flux.fromIterable(sliceBatch(entityList)).flatMap(batchList -> {
				Batch batch = connection.createBatch();
				for (S s : batchList) {
					Object id = ReflectionUtils.getFieldValue(s, idColumn.getJavaName());
					List<Condition> updateConditions = new ArrayList<>();
					updateConditions.add(new Condition(idColumn.getJavaName(), Operator.equal, id));
					updateConditions.addAll(conditions);
					Predicate<Column> predicate = includes == null ? new Filters.NonNullFilter(s) : new Filters.NonNullFilter(s).or(includes);
					String rawUpdate = r2dbcSqlBuilder.rawUpdate(s, table, updateConditions, predicate.and(Filters.UPDATABLE_FILTER));
					batch.add(rawUpdate);
				}
				return batch.execute();
			}).flatMap(Result::getRowsUpdated).collectList().map(rowsUpdatedList -> {
				int totalRowsUpdated = 0;
				for (Integer rowsUpdated : rowsUpdatedList) {
					totalRowsUpdated += rowsUpdated;
				}
				if (totalRowsUpdated != entityList.size()) {
					throw new R2dbcException("error batch update");
				}
				return totalRowsUpdated;
			});
		});
	}

	@Override
	public <S extends T> Flux<S> batchUpdateNonNullAnd(Iterable<S> entities, SFunction<T, ?>... columns) {
		List<String> includes = (columns == null || columns.length == 0) ? null : Arrays.stream(columns).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		return Flux.fromIterable(entities).flatMap(preUpdateFunction()).collectList().flatMapMany(entityList -> {
			return doBatchUpdateNonNull(entityList, includes == null ? null : new Filters.IncludeFilter(includes)).thenMany(Flux.fromIterable(entityList));
		}).flatMap(postUpdateFunction());
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
		return Flux.merge(Flux.just(new Condition(idColumn.getJavaName(), Operator.equal, id)), commonConditions())
				.collectList().map(conditions -> {
					return r2dbcSqlBuilder.update(entity, table, criteriaWithCondition(conditions).getConditions(), columnFilter.and(Filters.UPDATABLE_FILTER));
				}).flatMap(r2dbcQuery -> {
					return execute(r2dbcQuery).thenReturn(entity);
				});
	}

	@Override
	public <S extends T> Flux<S> batchUpdate(@NonNull Iterable<S> entities) {
		return Flux.fromIterable(entities).flatMap(preUpdateFunction()).collectList().flatMapMany(entityList -> {
			return doBatchUpdate(entityList, Filters.ALWAYS_TRUE_FILTER).thenMany(Flux.fromIterable(entityList));
		}).flatMap(postUpdateFunction());
	}

	private <S> Mono<Integer> doBatchUpdate(List<S> entityList, Predicate<Column> predicate) {
		return Mono.zip(commonConditions().collectList(), ConnectionFactoryUtils.getConnection(connectionFactory)).flatMap(tup -> {
			List<Condition> conditions = tup.getT1();
			Connection connection = tup.getT2();
			return Flux.fromIterable(sliceBatch(entityList)).flatMap(batchList -> {
				Batch batch = connection.createBatch();
				for (S s : batchList) {
					Object id = ReflectionUtils.getFieldValue(s, idColumn.getJavaName());
					List<Condition> updateConditions = new ArrayList<>();
					updateConditions.add(new Condition(idColumn.getJavaName(), Operator.equal, id));
					updateConditions.addAll(conditions);
					String rawUpdate = r2dbcSqlBuilder.rawUpdate(s, table, updateConditions, predicate.and(Filters.UPDATABLE_FILTER));
					batch.add(rawUpdate);
				}
				return batch.execute();
			}).flatMap(Result::getRowsUpdated).collectList().map(rowsUpdatedList -> {
				int totalRowsUpdated = 0;
				for (Integer rowsUpdated : rowsUpdatedList) {
					totalRowsUpdated += rowsUpdated;
				}
				if (totalRowsUpdated != entityList.size()) {
					throw new R2dbcException("error batch update");
				}
				return totalRowsUpdated;
			});
		});
	}

	@Override
	public <S extends T> Flux<S> batchUpdateWith(Iterable<S> entities, SFunction<T, ?>... columns) {
		if (columns == null || columns.length == 0) {
			return Flux.error(new R2dbcException("non columns to update"));
		}
		List<String> includes = Arrays.stream(columns).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		return Flux.fromIterable(entities).flatMap(preUpdateFunction()).collectList().flatMapMany(entityList -> {
			return doBatchUpdate(entityList, new Filters.IncludeFilter(includes)).thenMany(Flux.fromIterable(entities));
		}).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Flux<S> batchUpdateWithout(Iterable<S> entities, SFunction<T, ?>... columns) {
		List<String> excludes = columns == null ? null : Arrays.stream(columns).map(LambdaUtils::getPropFromLambda).collect(Collectors.toList());
		return Flux.fromIterable(entities).flatMap(preUpdateFunction()).collectList().flatMapMany(entityList -> {
			return doBatchUpdate(entityList, excludes == null ? Filters.ALWAYS_TRUE_FILTER : new Filters.ExcludeFilter(excludes))
					.thenMany(Flux.fromIterable(entityList));
		}).flatMap(postUpdateFunction());
	}

	@Override
	public <S extends T> Flux<S> batchUpdate(@NonNull Publisher<S> entityStream) {
		return Flux.from(entityStream).flatMap(preUpdateFunction()).collectList()
				.flatMapMany(entityList -> doBatchUpdate(entityList, Filters.ALWAYS_TRUE_FILTER).thenMany(Flux.fromIterable(entityList)))
				.flatMap(postUpdateFunction());
	}

	@Override
	public Mono<T> get(@NonNull ID id) {
		Criteria<T> criteria = Criteria.from(entityClass);
		criteria.getConditions().add(new Condition(idColumn.getJavaName(), Operator.equal, id));
		return this.findOne(criteria);
	}

	@Override
	public Mono<T> get(@NonNull Publisher<ID> idPublisher) {
		return Mono.from(idPublisher).map(id -> {
			Criteria<T> criteria = Criteria.from(entityClass);
			criteria.getConditions().add(new Condition(idColumn.getJavaName(), Operator.equal, id));
			return criteria;
		}).flatMap(this::findOne);
	}

	private Flux<Condition> commonConditions() {
		List<Field> fields = ReflectionUtils.getDeclaredFields(entityClass);
		Flux<Condition> flux = Flux.empty();
		for (Field field : fields) {
			if (field.getAnnotation(TenantId.class) != null) {
				flux = flux.mergeWith(ReactiveSystemContext.getSystemContext().map(context -> {
					return new Condition(field.getName(), Operator.equal, context.getTenantId());
				}));
			}
			if (field.getAnnotation(IsDeleted.class) != null) {
				Class<?> fieldType = field.getType();
				if (fieldType.getName().equals(Byte.class.getName()) || fieldType.getName().equals(byte.class.getName())) {
					flux = flux.mergeWith(Mono.just(new Condition(field.getName(), Operator.equal, (byte) 0)));
				} else if (fieldType.getName().equals(Integer.class.getName()) || fieldType.getName().equals(int.class.getName())) {
					flux = flux.mergeWith(Mono.just(new Condition(field.getName(), Operator.equal, 0)));
				} else {
					log.error("field type " + fieldType.getName() + " is neither byte nor int ,illegal type");
				}
			}
		}
		return flux;
	}

	@Override
	public Mono<Boolean> existsById(@NonNull ID id) {
		return Flux.merge(Flux.just(new Condition(idColumn.getJavaName(), Operator.equal, id)), commonConditions()).collectList().map(this::criteriaWithCondition).flatMap(this::count).map(count -> count > 0);
	}

	@Override
	public Mono<Boolean> existsById(@NonNull Publisher<ID> idPublisher) {
		return Flux.from(idPublisher).map(id -> {
			return new Condition(idColumn.getJavaName(), Operator.equal, id);
		}).mergeWith(commonConditions())
				.collectList().map(this::criteriaWithCondition).flatMap(this::count).map(count -> count > 0);
	}

	@Override
	public Mono<Integer> delete(@NonNull ID id) {
		Criteria<T> criteria = Criteria.from(entityClass);
		criteria.getConditions().add(new Condition(idColumn.getJavaName(), Operator.equal, id));
		return this.delete(criteria);
	}

	@Override
	public Mono<Integer> delete(@NonNull Publisher<ID> idPublisher) {
		return Mono.from(idPublisher).map(id -> {
			Criteria<T> criteria = Criteria.from(entityClass);
			criteria.getConditions().add(new Condition(idColumn.getJavaName(), Operator.equal, id));
			return criteria;
		}).flatMap(this::delete);
	}

	@Override
	public Mono<Integer> delete(@NonNull Criteria<T> criteria) {
		return Mono.zip(Mono.just(criteria).flatMap(crit -> {
					return commonConditions().collectList().map(conditions -> {
						crit.getConditions().addAll(conditions);
						return crit;
					}).defaultIfEmpty(crit);
				}
		), ReactiveSystemContext.getSystemContext()).flatMap(tup -> {
			Criteria<T> crit = tup.getT1();
			SystemContext systemContext = tup.getT2();
			if (CollectionUtils.isEmpty(crit.getConditions())) {
				return Mono.error(new RuntimeException("empty conditions to delete the entity ,this will ignore"));
			}
			if (CollectionUtils.isNotEmpty(preDeleteListeners)) {
				return this.find(crit).flatMap(preDeleteFunction()).collectList().flatMap(entityList -> {
					return execute(r2dbcSqlBuilder.delete(table, crit.getConditions(), systemContext));
				});
			} else {
				return execute(r2dbcSqlBuilder.delete(table, crit.getConditions(), systemContext));
			}
		});
	}

	private Criteria<T> criteriaWithCondition(List<Condition> conditions) {
		return mergeConditions(null, conditions);
	}

	private Criteria<T> mergeConditions(Criteria<T> criteria, List<Condition> conditions) {
		Criteria<T> merge = Criteria.from(entityClass);
		if (criteria != null) {
			merge.setSortProperties(criteria.getSortProperties());
			merge.setJoins(criteria.getJoins());
			merge.setSelector(criteria.getSelector());
			List<Condition> mergedConditions = criteria.getConditions().stream().map(Condition::copy).collect(Collectors.toList());
			mergedConditions.addAll(conditions);
			merge.setConditions(mergedConditions);
		} else {
			merge.setConditions(conditions);
		}
		return merge;
	}

	private <S extends T> Function<S, Mono<? extends S>> preDeleteFunction() {
		return entity -> {
			if (preDeleteListeners != null) {
				return Flux.fromIterable(preDeleteListeners).sort(Comparator.comparing(OrderedListener::getOrder)).flatMap(preDeleteListener -> {
					return preDeleteListener.preDelete(entity).onErrorStop().doOnError(err -> {
						log.error(err.getMessage(), err);
					});
				}).last();
			} else {
				return Mono.just(entity);
			}
		};
	}

	@Override
	public Flux<T> find(@NonNull Criteria<T> criteria) {
		return Flux.merge(Flux.fromIterable(criteria.getConditions()), commonConditions()).collectList().map(cnd -> {
			criteria.setConditions(cnd);
			return criteria;
		}).defaultIfEmpty(criteria).map(crt -> {
			return r2dbcSqlBuilder.find(table, crt);
		}).flatMapMany(this::query);
	}

	@Override
	public Mono<T> findOne(@NonNull Criteria<T> criteria) {
		PageRequest pageRequest = PageRequest.of(1, 1);
		pageRequest.setPaging(false);
		pageRequest.setCounting(false);
		return findPage(pageRequest, criteria).flatMap(pagination -> {
			return CollectionUtils.isEmpty(pagination.getRows()) ? Mono.empty() : Mono.just(pagination.getRows().get(0));
		});
	}

	@Override
	public Mono<Long> count(@NonNull Criteria<T> criteria) {
		List<Condition> conditions = criteria.getConditions() == null ? Collections.emptyList() : criteria.getConditions();
		return Flux.merge(Flux.fromIterable(conditions), commonConditions()).collectList().map(cnds -> {
			return mergeConditions(criteria, cnds);
		}).defaultIfEmpty(criteria)
				.flatMap(mergedCriteria -> {
					R2dbcQuery query = r2dbcSqlBuilder.count(table, mergedCriteria);
					DatabaseClient.GenericExecuteSpec exec = bind(query);
					return exec.map(row -> {
						Object count = row.get(0);
						if (count == null) {
							log.warn("error got count");
							return 0L;
						}
						return Long.valueOf(String.valueOf(count));
					}).one();
				});
	}

	@Override
	public <R> Mono<Long> count(SFunction<T, R> property, R value) {
		return count(Criteria.from(entityClass).and(property).is(value));
	}

	@Override
	public <R> Flux<T> findByProperty(SFunction<T, R> property, R value) {
		return find(Criteria.from(entityClass).and(property).is(value));
	}

	@Override
	public <R> Mono<Boolean> isRepeated(ID id, SFunction<T, R> property, R value) {
		Criteria<T> criteria = Criteria.from(entityClass);
		if (id != null) {
			Condition condition = new Condition();
			Column idColumn = table.getColumns().stream().filter(Column::isPk).findFirst().orElseThrow(() -> new R2dbcException("table has no id column"));
			condition.setPropertyName(idColumn.getJavaName());
			condition.setOperator(Operator.notEqual);
			condition.setValue(id);
			criteria.getConditions().add(condition);
		}
		return count(criteria.and(property).is(value)).map(count -> {
			return count > 0;
		});
	}

	@Override
	public Mono<Pagination<T>> findPage(@NonNull PageRequest pageRequest, @NonNull Criteria<T> criteria) {
		boolean paged = pageRequest.isPaging();
		if (paged) {
			int limit = pageRequest.getPageSize();
			long offset = pageRequest.getPageNumber() < 0 ? 0 : (pageRequest.getPageNumber() - 1) * pageRequest.getPageSize();
			if (pageRequest.isCounting()) {
				return Mono.zip(count(criteria), Mono.just(r2dbcSqlBuilder.findPage(table, criteria, limit, offset)).flatMap(r2dbcQuery -> {
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
				return Mono.just(r2dbcSqlBuilder.findPage(table, criteria, limit, offset)).flatMap(r2dbcQuery -> {
					return query(r2dbcQuery).collectList();
				}).map(rows -> {
					Pagination<T> pagination = new Pagination<>();
					pagination.setPageNo(pageRequest.getPageNumber());
					pagination.setPageSize(pageRequest.getPageSize());
					pagination.setTotal(-1);
					pagination.setRows(rows);
					return pagination;
				});
			}
		} else {
			return find(criteria).collectList().map(result -> {
				Pagination<T> pagination = new Pagination<>();
				pagination.setPageNo(1);
				pagination.setPageSize(result.size());
				pagination.setTotal(result.size());
				pagination.setRows(result);
				return pagination;
			});
		}
	}

	/**
	 * 按照动态条件查询记录，并且分页
	 * 如果对象中有{@link TenantId} 会自动增加 过滤条件
	 * 如果对象中有  {@link IsDeleted} 会自动增加过滤条件
	 *
	 * @param criteria 动态条件
	 * @return 分页查询结果
	 */
	@Override
	public Mono<Pagination<T>> findPage(@NonNull Criteria<T> criteria) {
		int limit = criteria.getLimit() == null ? 0 : criteria.getLimit();
		long offset = criteria.getOffset() == null ? 0 : criteria.getOffset();
		return Mono.zip(count(criteria), Mono.just(r2dbcSqlBuilder.findPage(table, criteria, limit, offset)).flatMap(r2dbcQuery -> {
			return query(r2dbcQuery).collectList();
		})).map(tup -> {
			Pagination<T> pagination = new Pagination<>();
			pagination.setTotal(tup.getT1());
			pagination.setRows(tup.getT2());
			return pagination;
		});
	}

	@Override
	public Flux<T> select(@NonNull String sql, @NonNull Map<String, R2dbcParam> params) {
		return query(new R2dbcQuery(sql, params));
	}

	@Override
	public Mono<Integer> execute(String sql, Map<String, R2dbcParam> params) {
		return execute(new R2dbcQuery(sql, params));
	}

	private Mono<Integer> execute(R2dbcQuery query) {
		DatabaseClient.GenericExecuteSpec bind = bind(query);
		return bind.fetch().rowsUpdated();
	}

	private DatabaseClient.GenericExecuteSpec bind(R2dbcQuery query) {
		DatabaseClient.GenericExecuteSpec execute = databaseClient.sql(query.getStatement());
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

	private <R> List<List<R>> sliceBatch(List<R> list) {
		if (list == null) {
			return null;
		}
		int size = list.size();
		if (size < batchSize) {
			return Collections.singletonList(list);
		}
		int batchListSize = size % batchSize == 0 ? size / batchSize : (size / batchSize) + 1;
		List<List<R>> batchList = new ArrayList<>(batchListSize);
		for (int i = 0; i < batchListSize; i++) {
			int startIndex = i * batchSize;
			int endIndex = (i + 1) * batchSize;
			endIndex = Math.min(endIndex, size);
			batchList.add(list.subList(startIndex, endIndex));
		}
		return batchList;
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

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
