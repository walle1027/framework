package org.loed.framework.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/13 下午9:57
 */
public class PoolExecutors {
	private static Map<String, ThreadPoolExecutor> poolExecutorMap = new ConcurrentHashMap<String, ThreadPoolExecutor>(12);
	private static Lock lock = new ReentrantLock();

	/**
	 * @see PoolExecutors#newThreadPool(java.lang.String, int, int, int, int)
	 */
	public static ThreadPoolExecutor newThreadPool(String name) {
		return newThreadPool(name, Integer.MAX_VALUE);
	}

	/**
	 * @see PoolExecutors#newThreadPool(java.lang.String, int, int, int, int)
	 */
	public static ThreadPoolExecutor newThreadPool(String name, int coreSize) {
		return newThreadPool(name, coreSize, coreSize);
	}

	/**
	 * @see PoolExecutors#newThreadPool(java.lang.String, int, int, int, int)
	 */
	public static ThreadPoolExecutor newThreadPool(String name, int coreSize, int maxSize) {
		return newThreadPool(name, coreSize, maxSize, Integer.MAX_VALUE);
	}

	/**
	 * @see PoolExecutors#newThreadPool(java.lang.String, int, int, int, int)
	 */
	public static ThreadPoolExecutor newThreadPool(String name, int coreSize, int maxSize, int queueCapacity) {
		return newThreadPool(name, coreSize, maxSize, queueCapacity, Integer.MAX_VALUE);
	}

	/**
	 * 创建新的线程池，如果线程池已经创建，返回已经创建的线程池
	 *
	 * @param name             线程组名称
	 * @param coreSize         java.util.concurrent.ThreadPoolExecutor's coreSize
	 * @param maxSize          java.util.concurrent.ThreadPoolExecutor's maxSize
	 * @param queueCapacity    the capacity of the java.util.concurrent.ThreadPoolExecutor's BlockingQueue
	 * @param keepAliveSeconds the java.util.concurrent.ThreadPoolExecutor's keep-alive seconds the default value is 60
	 * @return 线程池
	 */
	public static ThreadPoolExecutor newThreadPool(String name, int coreSize, int maxSize, int queueCapacity, int keepAliveSeconds) {
		if (!poolExecutorMap.containsKey(name)) {
			try {
				lock.lock();
				ThreadPoolExecutor threadPoolExecutor = createThreadPool(name, coreSize, maxSize, queueCapacity, keepAliveSeconds);
				poolExecutorMap.put(name, threadPoolExecutor);
			} finally {
				lock.unlock();
			}
		}
		return poolExecutorMap.get(name);
	}

	/**
	 * 创建新的线程池，如果线程池已经创建，返回已经创建的线程池
	 *
	 * @param name          线程组名称
	 * @param nThreads      java.util.concurrent.ThreadPoolExecutor's coreSize
	 * @param threadFactory the ThreadFactory to use for the ThreadPoolExecutor's thread pool
	 * @return 线程池
	 */
	public static ThreadPoolExecutor newThreadPool(String name, int nThreads, ThreadFactory threadFactory) {
		if (!poolExecutorMap.containsKey(name)) {
			try {
				lock.lock();
				ThreadPoolExecutor threadPoolExecutor = createThreadPool(name, nThreads, threadFactory);
				poolExecutorMap.put(name, threadPoolExecutor);
			} finally {
				lock.unlock();
			}
		}
		return poolExecutorMap.get(name);
	}

	private static ThreadPoolExecutor createThreadPool(String name, int coreSize, int maxSize, int queueCapacity, int keepAliveSeconds) {
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor();
		threadPoolExecutor.setCorePoolSize(coreSize);
		threadPoolExecutor.setMaxPoolSize(maxSize);
		threadPoolExecutor.setQueueCapacity(queueCapacity);
		threadPoolExecutor.setKeepAliveSeconds(keepAliveSeconds);
		threadPoolExecutor.setThreadGroupName(name);
		threadPoolExecutor.afterPropertiesSet();
		return threadPoolExecutor;
	}

	private static ThreadPoolExecutor createThreadPool(String name, int coreSize, ThreadFactory threadFactory) {
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor();
		threadPoolExecutor.setCorePoolSize(coreSize);
		threadPoolExecutor.setThreadFactory(threadFactory);
		threadPoolExecutor.afterPropertiesSet();
		threadPoolExecutor.setThreadGroupName(name);
		return threadPoolExecutor;
	}
}
