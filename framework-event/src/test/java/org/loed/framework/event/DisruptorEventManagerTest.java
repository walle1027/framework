package org.loed.framework.event;

import org.junit.Test;
import org.loed.framework.event.support.disruptor.DisruptorEventManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Thomason
 * @version 1.0
 * @since on 2017/1/21 17:18
 */
public class DisruptorEventManagerTest {

	@Test
	public void test1() throws Exception {
		final DisruptorEventManager eventManager = new DisruptorEventManager();
		final AtomicLong counter = new AtomicLong(0);
		final Long[] count = {0L};
		final ExecutorService executors = Executors.newFixedThreadPool(100);
		EventListener eventListener = new EventListener() {
			@Override
			public void execute(String eventName, final String context) {
//				counter.addAndGet(1);
				count[0]++;
				/*executors.execute(new Runnable() {
					@Override
					public void run() {
						String name = Thread.currentThread().getName();
//						System.out.println(name + ":" + context);
						long start = System.currentTimeMillis();
						for (int i = 0; i < 100; i++) {
							double pow = Math.pow(2, i);
						}
						long end = System.currentTimeMillis();
						System.out.println(end - start);
						counter.addAndGet(1);
					}
				});*/
			}
		};
		eventManager.subscribe("test", eventListener);
		eventManager.start("test");
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < Integer.MAX_VALUE; i++) {
					eventManager.publish("test", createRandomString(10000));
				}
			}
		}).start();
		long start = 0;
		while (true) {
			System.out.println("execute :[" + (count[0] - start) + "]times");
			start = count[0];
			Thread.sleep(1000L);
		}
	}

	private String createRandomString(int length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			builder.append(Math.round(1));
		}
		return builder.toString();
	}
}
