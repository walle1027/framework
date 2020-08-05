package org.loed.framework.r2dbc.listener.impl;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.r2dbc.listener.spi.PostInsertListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/31 4:37 下午
 */
@Slf4j
public class DefaultPostInsertListener implements PostInsertListener {
	@Override
	public <T> Mono<T> postInsert(T object) {
		log.info("DefaultPostInsertListener is invoked");
		return Mono.just(object);
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
