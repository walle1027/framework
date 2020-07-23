//package org.loed.framework.r2dbc;
//
//import io.r2dbc.pool.ConnectionPool;
//import io.r2dbc.spi.Connection;
//import io.r2dbc.spi.Statement;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.loed.framework.common.po.CommonPO;
//import org.loed.framework.common.util.ReflectionUtils;
//import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
//import org.reactivestreams.Publisher;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//import reactor.util.function.Tuple2;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.function.Function;
//
///**
// * @author thomason
// * @version 1.0
// * @since 2020/4/15 3:03 PM
// */
//@Slf4j
//public class BaseDAO<T extends CommonPO> {
//
//	private List<PreInsertListener> preInsertListeners;
//
//	private ConnectionPoolProxy connectionPoolProxy;
//
//	private String R2DBC_CONN = getClass().getName();
//
//	protected Mono<Connection> getConnection(ServerWebExchange exchange) {
//		if (exchange.getAttribute(R2DBC_CONN) != null) {
//			return Mono.just(Objects.requireNonNull(exchange.getAttribute(R2DBC_CONN)));
//		}
//		ConnectionPool connectionPool = connectionPoolProxy.getConnectionPool(exchange);
//		if (connectionPool == null) {
//			return Mono.error(new RuntimeException("error get connection pool"));
//		}
//		return connectionPool.create().flatMap(connection -> {
//			exchange.getAttributes().put(R2DBC_CONN, connection);
//			return Mono.from(connection.setAutoCommit(false)).then(Mono.from(connection.beginTransaction())).thenReturn(connection);
//		});
//	}
//
//	public Mono<Integer> insert(ServerWebExchange exchange, T po) {
//		if (CollectionUtils.isNotEmpty(preInsertListeners)) {
//			for (PreInsertListener preInsertListener : preInsertListeners) {
//				boolean insert = preInsertListener.preInsert(exchange, po);
//				if (!insert) {
//					return Mono.error(new R2dbcException()).then(Mono.just(-1));
//				}
//			}
//		}
//		Tuple2<String, Map<String, Object>> insert = R2dbcSqlBuilder.insert(po);
//		if (insert == null) {
//			return Mono.error(new R2dbcException("none exception")).then(Mono.just(-1));
//		}
//		return getConnection(exchange).flatMap(connection -> {
//			return Mono.from(connection.setAutoCommit(true))
//					.then(Mono.defer(() -> {
//						Statement statement = connection.createStatement(insert.getT1());
//						Map<String, Object> params = insert.getT2();
//						params.forEach((k, v) -> {
//							if (v == null) {
//								statement.bindNull(k, ReflectionUtils.getDeclaredField(po.getClass(), k).getType());
//							} else {
//								statement.bind(k, v);
//							}
//						});
//						return Mono.from(statement.execute());
//					}))
//					.flatMap(r -> Mono.from(r.getRowsUpdated()))
//					.onErrorMap(R2dbcException::new);
//		});
//	}
//
//
//	public Mono<Tuple2<Integer, T>> insert(Connection connection, ServerWebExchange exchange, T po) {
//		if (CollectionUtils.isNotEmpty(preInsertListeners)) {
//			for (PreInsertListener preInsertListener : preInsertListeners) {
//				boolean insert = preInsertListener.preInsert(exchange, po);
//				if (!insert) {
//					return Mono.zip(Mono.just(-1), Mono.just(po));
//				}
//			}
//		}
//		Statement statement = connection.createStatement("");
//		return Mono.from(statement.execute()).flatMap(r -> {
//			return Mono.zip(Mono.from(r.getRowsUpdated())
//					, Mono.from(r.map((row, meta) -> {
//						return po;
//					})));
//		});
//	}
//
//	public Mono<Void> commit(ServerWebExchange exchange) {
//		Connection connection = exchange.getAttribute(R2DBC_CONN);
//		if (connection == null) {
//			return Mono.empty();
//		}
//		return Mono.from(connection.commitTransaction()).doOnError(e -> {
//			log.error("error commit transaction caused by {}", e.getMessage(), e);
//		}).doFinally(f -> {
//			log.debug("successfully commit connection");
//		});
//	}
//
//	public Mono<Void> rollback(ServerWebExchange exchange) {
//		Connection connection = exchange.getAttribute(R2DBC_CONN);
//		if (connection == null) {
//			return Mono.empty();
//		}
//		return Mono.from(connection.rollbackTransaction()).doOnError(e -> {
//			log.error("error close connection  caused by {}", e.getMessage(), e);
//		}).doFinally(f -> {
//			log.debug("successfully rollback connection");
//		});
//	}
//
//	public Mono<Void> close(ServerWebExchange exchange) {
//		Connection connection = exchange.getAttribute(R2DBC_CONN);
//		if (connection == null) {
//			return Mono.empty();
//		}
//		return Mono.from(connection.close()).doOnError(e -> {
//			System.out.println("error close connection {} caused by {}");
//		}).doFinally(f -> {
//			System.out.println("successfully close connection");
//		});
//	}
//
//	public void setConnectionPoolProxy(ConnectionPoolProxy connectionPoolProxy) {
//		this.connectionPoolProxy = connectionPoolProxy;
//	}
//
//	public ConnectionPoolProxy getConnectionPoolProxy() {
//		return connectionPoolProxy;
//	}
//
//	public List<PreInsertListener> getPreInsertListeners() {
//		return preInsertListeners;
//	}
//
//	public void setPreInsertListeners(List<PreInsertListener> preInsertListeners) {
//		this.preInsertListeners = preInsertListeners;
//	}
//}
