package org.loed.framework.event;

import org.junit.Test;
import org.loed.framework.event.support.redis.RedisEventManager;
import org.loed.framework.event.support.redis.RedisEventProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/6/21 17:39
 */
public class RedisEventProcessorTest {
	private int threadsCount = 100;
	private ThreadPoolTaskExecutor _taskExecutor;
	private RedisEventManager _eventManager;
	private ReentrantLock lock = new ReentrantLock();

	private RedisEventProcessor createRedisEventProcessor() throws Exception {
		ThreadPoolTaskExecutor taskExecutor = getThreadPoolTaskExecutor();
		RedisEventProcessor redisEventProcessor = new RedisEventProcessor();
		redisEventProcessor.setMaxRetryCount(10);
		redisEventProcessor.setTaskExecutor(taskExecutor);
		return redisEventProcessor;
	}

	private ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
		if (_taskExecutor == null) {
			lock.lock();
			try {
				_taskExecutor = new ThreadPoolTaskExecutor();
				_taskExecutor.setCorePoolSize(10);
				_taskExecutor.setMaxPoolSize(50);
				_taskExecutor.setQueueCapacity(5);
				_taskExecutor.afterPropertiesSet();
			} finally {
				lock.unlock();
			}
		}
		return _taskExecutor;
	}

	private EventManager getEventManager() throws Exception {
		if (_eventManager == null) {
			lock.lock();
			try {
				_eventManager = new RedisEventManager();
			} finally {
				lock.unlock();
			}
		}
		return _eventManager;
	}

	@Test
	public void concurrentTest() throws Exception {
		for (int i = 0; i < threadsCount; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						RedisEventProcessor redisEventProcessor = createRedisEventProcessor();
						redisEventProcessor.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		long count = 0;
		long maxCount = 10000;
		String eventName = "test_test";
		EventManager eventManager = getEventManager();
		final List<String> executeList = new ArrayList<>();
		eventManager.subscribe(eventName, new EventListener() {
			@Override
			public void execute(String eventName, String context) {
				executeList.add(context);
			}
		});
		while (count < maxCount) {
			eventManager.publish(eventName, count + "");
			count++;
//			Thread.sleep(80);
		}
		while (maxCount > executeList.size()) {
			Thread.sleep(100);
		}
		System.out.println("execute over");
	}
}
