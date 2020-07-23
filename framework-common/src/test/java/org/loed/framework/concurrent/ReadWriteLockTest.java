package org.loed.framework.concurrent;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-1-28 上午11:51
 */

public class ReadWriteLockTest {
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private static ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
	private static ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	private static Map<String, String> maps = new HashMap<String, String>();
	private static CountDownLatch latch = new CountDownLatch(102);
	private static CyclicBarrier barrier = new CyclicBarrier(102);

	@Test
	public void testReadWriteLock() throws Exception {
		long beginTime = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			new Thread(new ReadThread()).start();
		}
		for (int i = 0; i < 2; i++) {
			new Thread(new WriteThread()).start();
		}
		latch.await();
		long endTime = System.currentTimeMillis();
		System.out.println("cost:[" + (endTime - beginTime) + "]millseconds");
	}

	private class ReadThread implements Runnable {
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
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				writeLock.lock();
				maps.put("1", "2");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				writeLock.unlock();
			}
			latch.countDown();
		}
	}

	private class WriteThread implements Runnable {
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
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				readLock.lock();
				maps.get("1");
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				readLock.unlock();
			}
			latch.countDown();
		}
	}

}
