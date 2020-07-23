package org.loed.framework.concurrent;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-1-18 下午5:52
 */

public class CyclicBarrierTest {
	@Test
	public void testCyclicBarrier() throws Exception {
		CyclicBarrier barrier = new CyclicBarrier(3);
		ExecutorService executor = Executors.newFixedThreadPool(3);
		executor.execute(new Thread(new Runner(barrier, "1号选手")));
		executor.execute(new Thread(new Runner(barrier, "2号选手")));
		executor.execute(new Thread(new Runner(barrier, "3号选手")));
		Thread.sleep(10000);
//		executor.shutdown();
	}

	class Runner implements Runnable {
		// 一个同步辅助类，它允许一组线程互相等待，直到到达某个公共屏障点 (common barrier point)
		private CyclicBarrier barrier;

		private String name;

		public Runner(CyclicBarrier barrier, String name) {
			super();
			this.barrier = barrier;
			this.name = name;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1000 * (new Random()).nextInt(8));
				System.out.println(name + " 准备好了...");
				// barrier的await方法，在所有参与者都已经在此 barrier 上调用 await 方法之前，将一直等待。
				barrier.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			System.out.println(sdf.format(new Date()) + " " + name + " 起跑！");
		}
	}
}


