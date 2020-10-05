package org.loed.framework.r2dbc.test.listener.spi;

import org.loed.framework.r2dbc.test.listener.OrderedListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:38
 */
public interface PreDeleteListener extends OrderedListener {
	/**
	 * 对象删除前的监听器，通常用来将带删除的对象放到回收站
	 *
	 * @param object 对象
	 * @param <T>    对象类型
	 * @return 待删除的对象
	 */
	<T> Mono<T> preDelete(T object);
}
