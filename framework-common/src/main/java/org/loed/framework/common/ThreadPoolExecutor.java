package org.loed.framework.common;

import org.loed.framework.common.context.SystemContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/8/22 0:06
 */

public class ThreadPoolExecutor extends ThreadPoolTaskExecutor {

	/**
	 * 在执行异步线程之前，将当前线程的线程变量(SystemContext.getContextMap())
	 * 复制到runnable执行的线程中
	 *
	 * @see org.springframework.core.task.AsyncTaskExecutor#submit(java.lang.Runnable)
	 */
	@Override
	public void execute(final Runnable task) {
		final Map<String, String> contextMap = SystemContext.getContextMap();
		RunProxy wrapper = new RunProxy(contextMap, task);
		super.execute(wrapper);
	}

	/**
	 * 在执行异步线程之前，将当前线程的线程变量(SystemContext.getContextMap())
	 * 复制到runnable执行的线程中
	 *
	 * @see org.springframework.core.task.AsyncTaskExecutor#execute(java.lang.Runnable, long)
	 */
	@Override
	public void execute(final Runnable task, long startTimeout) {
		final Map<String, String> contextMap = SystemContext.getContextMap();
		RunProxy wrapper = new RunProxy(contextMap, task);
		super.execute(wrapper, startTimeout);
	}

	/**
	 * 在执行异步线程之前，将当前线程的线程变量(SystemContext.getContextMap())
	 * 复制到runnable执行的线程中
	 *
	 * @see org.springframework.core.task.AsyncTaskExecutor#submit(java.lang.Runnable)
	 */
	@Override
	public Future<?> submit(final Runnable task) {
		final Map<String, String> contextMap = SystemContext.getContextMap();
		RunProxy wrapper = new RunProxy(contextMap, task);
		return super.submit(wrapper);
	}

	/**
	 * 在执行异步线程之前，将当前线程的线程变量(SystemContext.getContextMap())
	 * 复制到runnable执行的线程中
	 *
	 * @see org.springframework.core.task.AsyncTaskExecutor#submit(java.util.concurrent.Callable)
	 */
	@Override
	public <T> Future<T> submit(final Callable<T> task) {
		final Map<String, String> contextMap = SystemContext.getContextMap();
		CallProxy<T> wrapper = new CallProxy<T>(contextMap, task);
		return super.submit(wrapper);
	}


	private static class RunProxy implements Runnable {
		private Map<String, String> contextMap;
		private Runnable runner;

		public RunProxy(Map<String, String> contextMap, Runnable runner) {
			if (contextMap != null) {
				this.contextMap = new HashMap<>();
				this.contextMap.putAll(contextMap);
			}
			this.runner = runner;
		}

		@Override
		public void run() {
			try {
				SystemContext.setContextMap(contextMap);
				runner.run();
			} finally {
				SystemContext.clean();
			}
		}
	}

	private static class CallProxy<T> implements Callable<T> {
		private Map<String, String> contextMap;
		private Callable<T> caller;

		public CallProxy(Map<String, String> contextMap, Callable<T> caller) {
			if (contextMap != null) {
				this.contextMap = new HashMap<>();
				this.contextMap.putAll(contextMap);
			}
			this.caller = caller;
		}

		@Override
		public T call() throws Exception {
			try {
				SystemContext.setContextMap(contextMap);
				return caller.call();
			} finally {
				SystemContext.clean();
			}
		}
	}
}
