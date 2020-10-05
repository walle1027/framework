package org.loed.framework.r2dbc.test.listener;

import org.loed.framework.r2dbc.test.listener.spi.PostUpdateListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 10:07 上午
 */
public class TestPostUpdateListener implements PostUpdateListener {
	@Override
	public <T> Mono<T> postUpdate(T object) {
		return null;
	}
}
