package org.loed.framework.event;

/**
 * @author Thomason
 * @version 1.0
 * @since 12-3-15 上午11:24
 */

public interface EventManager {

	/**
	 * 订阅事件
	 * 默认订阅的事件为同步事件
	 *
	 * @param eventName 事件主题
	 *                  事件主题的一般格式为:公司代码/应用代码/功能代码/事件描述关键字
	 *                  例如:default/wms/inbound/inStock
	 * @param listener  事件监听
	 */
	void subscribe(final String eventName, final EventListener listener);

	/**
	 * 事件触发
	 *
	 * @param topic   事件主题
	 *                事件主题的一般格式为:公司代码/应用代码/功能代码/事件描述关键字
	 *                例如:default/wms/inbound/inStock
	 * @param context 事件发布的参数
	 *                context 的实现类必须实现java.io.Serializable 接口
	 *                context 中的key和value都必须实现java.io.Serializable 接口
	 */
	void publish(final String topic, final String context);
}
