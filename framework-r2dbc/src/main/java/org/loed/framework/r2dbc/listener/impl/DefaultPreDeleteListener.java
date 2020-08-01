package org.loed.framework.r2dbc.listener.impl;

import org.loed.framework.r2dbc.listener.spi.PreDeleteListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:47
 */
public class DefaultPreDeleteListener implements PreDeleteListener {

	@Override
	public Mono<Void> preDelete(Object object) {
		return Mono.just(object).then();
	}
}
