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
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.GenerationType;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/7 3:20 下午
 */
public class DefaultR2DbcDao<T, ID> implements R2dbcDao<T, ID> {
	private DatabaseClient databaseClient;

	private final Class<? extends R2dbcDao<T, ID>> daoInterface;

	private List<PreInsertListener> preInsertListeners;

	private final Class<T> entityClass;

	private final Class<ID> idClass;

	private final JPAClassRowMapper<T> mapper;

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
			return Flux.fromIterable(preInsertListeners)
					.flatMap(preInsertListener -> {
						return preInsertListener.preInsert(entity);
					}).any(canInsert -> !canInsert).flatMap(notInsert -> {
						if (notInsert) {
							return Mono.error(new RuntimeException("can't insert "));
						}
						return doInsert(table, entity);
					});
		}
		return doInsert(table, entity);
	}

	public <S extends T> Mono<S> doInsert(Table table, S entity) {
		Optional<Column> idColumn = table.getColumns().stream().filter(Column::isPk).findFirst();
		if (!idColumn.isPresent()) {
			return Mono.error(new NoSuchFieldError("id property"));
		}
		DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = databaseClient.insert().into(table.getSqlName());
		List<Column> columns = table.getColumns().stream().filter(Column::isInsertable).collect(Collectors.toList());
		for (Column column : columns) {
			Object value = ReflectionUtils.getFieldValue(entity, column.getJavaName());
			if (value == null) {
				//TODO 处理默认值
				insertSpec = insertSpec.nullValue(SqlIdentifier.quoted(column.getSqlName()), column.getJavaType());
			} else {
				insertSpec = insertSpec.value(SqlIdentifier.quoted(column.getSqlName()), value);
			}
		}
		GenerationType idGenerationType = table.getIdGenerationType();
		if (idGenerationType.equals(GenerationType.AUTO)) {
			return insertSpec.map(row -> {
				//TODO check the auto increase Id
				return (ID) row.get("insertedId");
			}).one().map(id -> {
				//set the id
				ReflectionUtils.setFieldValue(entity, idColumn.get().getJavaName(), id);
				return entity;
			});
		} else {
			return insertSpec.fetch().one().map(row -> {
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
//		databaseClient.update().table("").using(Update.update())
		return null;
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
