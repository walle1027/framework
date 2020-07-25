package org.loed.framework.r2dbc.dao;

import org.loed.framework.common.ORMapping;
import org.loed.framework.common.database.Column;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.lambda.SFunction;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.persistence.GenerationType;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/7 3:20 下午
 */
@SuppressWarnings("unchecked")
public class DefaultR2DbcDao<T, ID> implements R2dbcDao<T, ID> {
	private final DatabaseClient databaseClient;

	private final Class<? extends R2dbcDao<T, ID>> daoInterface;

	private final Class<T> entityClass;

	private final Class<ID> idClass;

	private final JPAClassRowMapper<T> mapper;

	public static final String BLANK = " ";

	private R2dbcSqlBuilder sqlbuilder;

	private List<PreInsertListener> preInsertListeners;

	public DefaultR2DbcDao(Class<? extends R2dbcDao<T, ID>> daoInterface, DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
		this.daoInterface = daoInterface;
		this.entityClass = (Class<T>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[0];
		this.idClass = (Class<ID>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[1];
		this.mapper = new JPAClassRowMapper<>(entityClass);
	}

	@Override
	public <S extends T> Mono<S> insert(S entity) {
		Table table = ORMapping.get(entityClass);
		if (table == null) {
			return Mono.error(new NoSuchFieldError(entityClass.getName()));
		}
		if (preInsertListeners != null) {
//			return Flux.fromIterable(preInsertListeners)
//					.flatMap(preInsertListener -> {
//						return preInsertListener.preInsert(entity);
//					}).any(canInsert -> !canInsert).flatMap(notInsert -> {
//						if (notInsert) {
//							return Mono.error(new RuntimeException("can't insert "));
//						}
//						return doInsert(table, entity);
//					});
		}
		return doInsert(table, entity);
	}

	public <S extends T> Mono<S> doInsert(Table table, S entity) {
		Optional<Column> idColumn = table.getColumns().stream().filter(Column::isPk).findFirst();
		if (!idColumn.isPresent()) {
			return Mono.error(new NoSuchFieldError("id property"));
		}
		Tuple2<String, List<Tuple2<String, Class<?>>>> insert = sqlbuilder.insert(table);
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
			return execute.fetch().one().map(row -> {
				return entity;
			});
		}
	}

	@Override
	public <S extends T> Flux<S> batchInsert(Iterable<S> entities) {
		return null;
	}

	@Override
	public <S extends T> Flux<S> batchInsert(Publisher<S> entityStream) {
		return null;
	}

	@Override
	public <S extends T> Mono<S> update(S entity) {
		Table table = ORMapping.get(entityClass);
		if (table == null) {
			return Mono.error(new RuntimeException("not a jpa table"));
		}
		Optional<Column> idColumn = table.getColumns().stream().filter(Column::isPk).findFirst();
		if (!idColumn.isPresent()) {
			return Mono.error(new RuntimeException("no id column"));
		}
		ID id = (ID) ReflectionUtils.getFieldValue(entity, idColumn.get().getJavaName());
		if (id == null) {
			return Mono.error(new RuntimeException("id is null "));
		}
		Tuple2<String, List<Tuple2<String, Class<?>>>> update = sqlbuilder.update(table);
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
	public <S extends T> Mono<S> updateWith(S entity, SFunction<T, ?>... columns) {
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
	public Mono<Void> deleteByCriteria(Criteria criteria) {
		return null;
	}

	@Override
	public Flux<T> find(Criteria criteria) {
		return null;
	}

	@Override
	public Mono<T> findOne(Criteria criteria) {
		return null;
	}

	@Override
	public Mono<Long> count(Criteria criteria) {
		return null;
	}

	@Override
	public Flux<T> findPage(Criteria criteria, PageRequest pageRequest) {
		return null;
	}

	@Override
	public Flux<T> select(String sql) {
		return null;
	}


	public void setPreInsertListeners(List<PreInsertListener> preInsertListeners) {
		this.preInsertListeners = preInsertListeners;
	}
}
