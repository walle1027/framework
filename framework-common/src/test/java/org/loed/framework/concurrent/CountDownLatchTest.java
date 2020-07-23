package org.loed.framework.concurrent;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-1-18 下午4:35
 */

public class CountDownLatchTest {
	@Test
	public void testCountDown() throws Exception {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd ss.SSS");
		System.out.println("all task start at:" + sf.format(new Date()));
		int threadCount = 100;
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(new Runner(countDownLatch));
			t.start();
		}
		countDownLatch.await();
		System.out.println("all task finish at:" + sf.format(new Date()));
	}

	private class Runner implements Runnable {
		private CountDownLatch countDownLatch;

		private Runner(CountDownLatch countDownLatch) {
			this.countDownLatch = countDownLatch;
		}

		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's
		 * <code>run</code> method to be called in that separately executing
		 * thread.
		 * <p/>
		 * The general contract of the method <code>run</code> is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		@Override
		public void run() {
			try {
				System.out.println("i am running...");
				Thread.sleep(2000);
				System.out.println("i am down " + Thread.currentThread().getName());
				countDownLatch.countDown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}
