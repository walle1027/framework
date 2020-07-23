package org.loed.framework;

import org.junit.Test;
import org.loed.framework.common.context.SystemContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/8/26 13:34
 */

public class ThreadLocalTest {

	int maxExecCount = 100000;

	@Test
	public void testThreadLocal() throws Exception {
		ExecutorService executorService = Executors.newCachedThreadPool();
		final CountDownLatch countDownLatch = new CountDownLatch(maxExecCount);
		for (int i = 0; i < maxExecCount; i++) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						SystemContext.setTenantCode("safdasdf");
						Thread.sleep(10L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						countDownLatch.countDown();
						SystemContext.clean();
					}
				}
			});
		}
		countDownLatch.await();
	}
}
