package org.loed.framework.event.support.redis;

import org.apache.commons.collections4.CollectionUtils;
import org.loed.framework.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Thomason
 * @version 1.0
 * @since 2014/10/13 15:08
 */

public class RedisEventProcessor implements InitializingBean, DisposableBean, Closeable {
	private Logger logger = LoggerFactory.getLogger(RedisEventProcessor.class);
	/**
	 * 连接中断后，重新尝试连接的间隔
	 */
	private static final int SUBSCRIBE_RETRY_WAIT_TIME_MILLIS = 5000;
	/**
	 * 任务队列满了以后，重新尝试执行任务的间隔
	 */
	private static final int TASK_EXECUTE_WAIT_TIME_MILLIS = 2000;
	/**
	 * 监听任务轮询间隔
	 */
	private static final int SLEEP_INTERVAL = 1;
	/**
	 * 轮询任务启动标志
	 */
	protected AtomicBoolean running = new AtomicBoolean(false);
	/**
	 * redis一致hash实现，多个key可以由一致hash实现进行key分配
	 */
	protected StringRedisTemplate redisTemplate;
	/**
	 * 任务执行异常时，重新尝试执行次数
	 */
	private int maxRetryCount = 10;
	/**
	 * 本地任务执行线程池
	 */
	private ThreadPoolTaskExecutor taskExecutor;
	/**
	 * 重试任务的redis前缀
	 */
	private static final String RETRY_PREFIX = "evt_retry:";
	/**
	 * 重试失败后丢弃的任务队列前缀
	 */
	private static final String DROPPED_EVENT_KEY = "dropped_evt:";

	public RedisEventProcessor() {
	}

	/**
	 * 启用redis队列监听<br>
	 * 目前采用的时轮询的方式，性能不是很好，可以采用监听的方式来实现，具体要调研
	 * redis.clients.jedis.Jedis#subscribe(redis.clients.jedis.JedisPubSub, java.lang.String...)
	 *
	 * @throws InterruptedException 线程中断异常
	 */
	public void start() throws InterruptedException {
		while (running.get()) {
			try {
				Set<String> eventNameSet = RedisEventManager.EVENT_CONTAINER.keySet();
				if (CollectionUtils.isNotEmpty(eventNameSet)) {
					final CountDownLatch latch = new CountDownLatch(eventNameSet.size());
					for (final String eventName : eventNameSet) {
						try {
							String userKey = RedisEventManager.EVENT_PREFIX + eventName;
							if (redisTemplate == null) {
								continue;
							}
							final String value = redisTemplate.opsForList().rightPop(userKey);
							if (value == null) {
								continue;
							}
							//当线程队列满的时候，当前线程阻塞，直到线程空闲
							executeEvent(eventName, value);
						} catch (RedisConnectionFailureException e) {
							if (running.get()) {
								logger.error("Lost connection to Sentinel at " + ". Sleeping 5000ms and retrying.");
								try {
									Thread.sleep(SUBSCRIBE_RETRY_WAIT_TIME_MILLIS);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
							} else {
								logger.error("server close !");
							}
						} finally {
							latch.countDown();
						}
					}
					latch.await();
				}
				Thread.sleep(SLEEP_INTERVAL);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}


	private void executeEvent(final String eventName, final String context) throws InterruptedException {
		logger.debug("execute event:" + eventName + " context:" + context);
		while (true) {
			try {
				taskExecutor.execute(new Runnable() {
					@Override
					public void run() {
						List<EventListener> listenerList = RedisEventManager.EVENT_CONTAINER.get(eventName);
						if (CollectionUtils.isNotEmpty(listenerList)) {
							for (EventListener listener : listenerList) {
								try {
									listener.execute(eventName, context);
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
									//执行失败时，将任务重新放到队列中
									final String retryKey = RETRY_PREFIX + eventName;
									String s = redisTemplate.opsForValue().get(retryKey);
									int retryCount = 0;
									if (s != null) {
										retryCount = Integer.valueOf(s);
									}
									//如果没有超过最大重试次数，将任务再次放入队列中
									if (retryCount < maxRetryCount) {
										//重试次数*秒后，将任务再次放入队列中
										final int finalRetryCount = retryCount++;
										FutureTask<Long> futureTask = new FutureTask<>(() -> {
											if (logger.isDebugEnabled()) {
												logger.debug("event:" + eventName + " put into queue[" + finalRetryCount + "times");
											}
											Long rpush = redisTemplate.opsForList().rightPush(eventName, context);
											redisTemplate.opsForValue().set(retryKey, finalRetryCount + "");
											return rpush;
										});
										try {
											taskExecutor.submit(futureTask);
											futureTask.get(retryCount, TimeUnit.SECONDS);
										} catch (InterruptedException | ExecutionException | TimeoutException e1) {
											e1.printStackTrace();
										} catch (TaskRejectedException e2) {
											if (logger.isWarnEnabled()) {
												logger.warn("taskExecutor is full,the event(name=" + eventName + ",context=" + context + ")will been dropped");
											}
											dropEvent(eventName, context);
										}
									}
									//如果超过最大重试次数，将任务放入丢弃任务列表中
									else {
										dropEvent(eventName, context);
									}
								}
							}
						}
					}
				});
				break;
			} catch (TaskRejectedException tre) {
				Thread.sleep(TASK_EXECUTE_WAIT_TIME_MILLIS);
			}
		}
	}

	private void dropEvent(String eventName, final String context) {
		if (logger.isDebugEnabled()) {
			logger.debug("event:" + eventName + " was been put into dropped map");
		}
		redisTemplate.opsForHash().put(DROPPED_EVENT_KEY, eventName, context);
	}

	public ThreadPoolTaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public StringRedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	/**
	 * Invoked by a BeanFactory after it has set all bean properties supplied
	 * (and satisfied BeanFactoryAware and ApplicationContextAware).
	 * <p>This method allows the bean instance to perform initialization only
	 * possible when all bean properties have been set and to throw an
	 * exception in the event of misconfiguration.
	 *
	 * @throws Exception in the event of misconfiguration (such
	 *                   as failure to set an essential property) or if initialization fails.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		running.set(true);
		Thread thread = new Thread(() -> {
			try {
				start();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.setName("redis queue watch thread");
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void destroy() throws Exception {
		this.close();
	}

	@Override
	public void close() throws IOException {
		running.set(false);
		if (taskExecutor != null) {
			taskExecutor.destroy();
		}
	}
}
