package org.loed.framework.event.support.redis;

import org.loed.framework.event.EventListener;
import org.loed.framework.event.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Thomason
 * @version 1.0
 * @since 12-3-15 上午11:24
 */

public final class RedisEventManager implements EventManager {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static final String EVENT_PREFIX = "EVT_";
	/**
	 * 事件容器
	 */
	public static final ConcurrentMap<String, List<EventListener>> EVENT_CONTAINER = new ConcurrentHashMap<String, List<EventListener>>();

	protected StringRedisTemplate redisTemplate;


	/**
	 * 私有的构造方法
	 */
	public RedisEventManager() {

	}

	/**
	 * 订阅事件
	 * 默认订阅的事件为同步事件
	 *
	 * @param eventName 事件主题
	 *                  事件主题的一般格式为:公司代码/应用代码/功能代码/事件描述关键字
	 *                  例如:default/wms/inbound/inStock
	 * @param listener  事件监听
	 */
	@Override
	public void subscribe(final String eventName, final EventListener listener) {
		EVENT_CONTAINER.computeIfAbsent(eventName, e -> new ArrayList<>());
		EVENT_CONTAINER.get(eventName).add(listener);
	}

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
	@Override
	public void publish(final String topic, final String context) {
		redisTemplate.opsForList().leftPush(EVENT_PREFIX + topic, context == null ? "" : context);
	}

	public StringRedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
}
