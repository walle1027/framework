package org.loed.framework.r2dbc.listener.spi;

import org.loed.framework.r2dbc.listener.OrderedListener;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:26
 */
public interface PreInsertListener extends OrderedListener {
	/**
	 * 对象新增前的监听器，可以在对象写入数据库前做属性修改和计算
	 *
	 * @param object 对象
	 * @param <T>    对象类型
	 * @return 修改后的对象
	 */
	<T> Mono<T> preInsert(T object);
}
