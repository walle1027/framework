package org.loed.framework.r2dbc.listener.spi;

import org.loed.framework.r2dbc.listener.OrderedListener;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:38
 */
public interface PostDeleteListener extends OrderedListener {
	Mono<Void> postDelete(ServerWebExchange exchange, Object object);
}
