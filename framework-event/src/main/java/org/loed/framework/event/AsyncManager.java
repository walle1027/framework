package org.loed.framework.event;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步处理类，先将任务放到线程池中执行，当线程池满的时候，自动把任务放入异步队列中
 * 异步队列如果执行失败超过10次，将任务丢弃，人工处理
 *
 * @author Thomason
 * @version 1.0
 * @since 2016/6/6 14:25
 */
public class AsyncManager {
	//异步事件处理器
	private EventManager eventManager;
	//线程池
	private ThreadPoolTaskExecutor taskExecutor;

	public void asyncExecute(final AsyncExecutor asyncExecutor) {
		try {
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					asyncExecutor.execute();
				}
			});
		} catch (TaskRejectedException e) {
			String eventName = "_AsyncTask_";
			eventManager.subscribe(eventName, new EventListener() {
				@Override
				public void execute(String eventName, String context) {
					asyncExecutor.execute();
				}
			});
			eventManager.publish(eventName, null);
		}
	}

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public interface AsyncExecutor {
		void execute();
	}
}
