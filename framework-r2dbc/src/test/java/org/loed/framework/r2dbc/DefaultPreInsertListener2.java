package org.loed.framework.r2dbc;

import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/2 9:08 上午
 */
public class DefaultPreInsertListener2 implements PreInsertListener {
	@Override
	public <T> Mono<T> preInsert(T object) {
		return Mono.just(object).map(obj -> {
			ReflectionUtils.setFieldValue(obj, "version", 2L);
			return obj;
		});
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
