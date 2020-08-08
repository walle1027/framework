package org.loed.framework.r2dbc.listener;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.r2dbc.listener.spi.PreDeleteListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 11:00 上午
 */
@Slf4j
public class TestPreDeleteListener implements PreDeleteListener {
	@Override
	public <T> Mono<T> preDelete(T object) {
		return Mono.just(object).doOnNext(po -> {
			log.info("preDelete object:" + po);
		});
	}
}
