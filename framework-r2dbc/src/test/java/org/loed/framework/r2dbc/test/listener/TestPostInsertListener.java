package org.loed.framework.r2dbc.test.listener;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.r2dbc.test.listener.spi.PostInsertListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 10:05 上午
 */
@Slf4j
public class TestPostInsertListener implements PostInsertListener {
	@Override
	public <T> Mono<T> postInsert(T object) {
		return Mono.just(object).doOnNext(obj -> {
			log.info("post insert on object:" + obj);
		});
	}
}
