package org.loed.framework.r2dbc.listener.spi;

import org.loed.framework.r2dbc.listener.OrderedListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:26
 */
public interface PreInsertListener extends OrderedListener {
	<T> Mono<T> preInsert(T object);
}
