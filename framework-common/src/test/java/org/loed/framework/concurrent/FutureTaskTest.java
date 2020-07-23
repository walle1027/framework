package org.loed.framework.concurrent;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-1-18 下午4:44
 */

public class FutureTaskTest {
	@Test
	public void testFutureTask() throws Exception {
		Callable<Integer> c1 = new Calller(10);
		Callable<Integer> c2 = new Calller(20);
		Callable<Integer> c3 = new Calller(30);
		FutureTask<Integer> ft1 = new FutureTask<Integer>(c1);
		FutureTask<Integer> ft2 = new FutureTask<Integer>(c2);
		FutureTask<Integer> ft3 = new FutureTask<Integer>(c3);
		new Thread(ft1).start();
		new Thread(ft2).start();
		new Thread(ft3).start();
		System.out.println("main thread do something else....1");
		Thread.sleep(100);
		System.out.println("main thread do something else....2");
		Thread.sleep(100);
		System.out.println("main thread do something else....3");
		Thread.sleep(100);
		System.out.println("main thread do something else....4");
		Thread.sleep(100);
		System.out.println("future task result :" + ft1.get() + "-" + ft2.get() + "-" + ft3.get());
	}

	private class Calller implements Callable<Integer> {
		private int step = 10;
		private int start;

		private Calller(int start) {
			this.start = start;
		}

		/**
		 * Computes a result, or throws an exception if unable to do so.
		 *
		 * @return computed result
		 * @throws Exception if unable to compute a result
		 */
		@Override
		public Integer call() throws Exception {
			Thread.sleep(3000);
			System.out.println("caller running with start:" + start);
			return start + step;
		}
	}
}
