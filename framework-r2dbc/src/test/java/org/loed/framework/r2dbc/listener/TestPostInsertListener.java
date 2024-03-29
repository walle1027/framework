package org.loed.framework.r2dbc.listener;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.r2dbc.listener.spi.PostInsertListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 10:05 上午
 */
@Slf4j
public class TestPostInsertListener implements PostInsertListener {
	private final int order;

	public TestPostInsertListener(int order) {
		this.order = order;
	}

	@Override
	public <T> Mono<T> postInsert(T object) {
		return Mono.just(object).doOnNext(obj -> {
			log.debug("with order " + order + " post insert on object:" + obj);
		});
	}

	@Override
	public int getOrder() {
		return order;
	}
}
