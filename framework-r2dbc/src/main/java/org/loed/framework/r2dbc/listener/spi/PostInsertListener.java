package org.loed.framework.r2dbc.listener.spi;

import org.loed.framework.r2dbc.listener.OrderedListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:26
 */
public interface PostInsertListener extends OrderedListener {
	/**
	 * 对象新增后的监听器，可以在对象返回前做一些属性修改
	 *
	 * @param object 对象
	 * @param <T>    对象类型
	 * @return 修改后的对象
	 */
	<T> Mono<T> postInsert(T object);
}
