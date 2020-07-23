package org.loed.framework.event;

/**
 * @author Thomason
 * @version 1
 * @since 11-7-12 上午10:32
 */
public interface EventListener {
	/**
	 * 事件执行器
	 *
	 * @param eventName 事件名称
	 * @param context   事件参数
	 */
	void execute(final String eventName, String context);
}
