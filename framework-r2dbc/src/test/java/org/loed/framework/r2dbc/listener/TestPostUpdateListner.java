package org.loed.framework.r2dbc.listener;

import org.loed.framework.r2dbc.listener.spi.PostUpdateListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 10:07 上午
 */
public class TestPostUpdateListner implements PostUpdateListener {
	@Override
	public <T> Mono<T> postUpdate(T object) {
		return null;
	}
}
