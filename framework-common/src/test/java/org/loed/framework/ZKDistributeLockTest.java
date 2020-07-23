package org.loed.framework;

import org.junit.Test;
import org.loed.framework.common.lock.ZKDistributeLock;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/9/8 9:35
 */

public class ZKDistributeLockTest {
	private String groupName = "test";
	private String sd = "test";

	@Test
	public void testMultiRun() throws Exception {
		for (int i = 0; i < 10; i++) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					final ZKDistributeLock zkDistributeLock = new ZKDistributeLock("www.dev.com:2181");
					boolean canRun = zkDistributeLock.accept(groupName + "/" + sd, (p) -> {
						System.out.println(Thread.currentThread().getName());
					});
				}
			};

			Thread t = new Thread(runnable);
			t.start();
		}
		Thread.sleep(10000L);
	}
}
