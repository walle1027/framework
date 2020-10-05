package org.loed.framework.r2dbc.test.listener.spi;

import org.loed.framework.r2dbc.test.listener.OrderedListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:26
 */
public interface PreUpdateListener extends OrderedListener {
	/**
	 * 对象修改前的监听器，可以在对象修改前做一些属性修改
	 *
	 * @param object 对象
	 * @param <T>    对象类型
	 * @return 修改后的对象
	 */
	<T> Mono<T> preUpdate(T object);
}
