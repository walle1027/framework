package org.loed.framework.common.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.loed.framework.common.util.StringHelper;
import org.loed.framework.common.zookeeper.ZKHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/9/7 18:57
 */

public class ZKDistributeLock {
	private static Logger logger = LoggerFactory.getLogger(ZKDistributeLock.class);
	private final CuratorFramework zkClient;
	private final String LOCK_PATH = "/lock";


	public ZKDistributeLock(String zkAddress) {
		this.zkClient = ZKHolder.get(zkAddress);
	}

	public ZKDistributeLock(CuratorFramework zkClient) {
		this.zkClient = zkClient;
	}

	/**
	 * 获取到分布式锁，并且运行代码
	 *
	 * @return true 获取到锁，并且运行了代码,false 未获取到锁
	 */
	public boolean accept(String lockPath, long time, TimeUnit unit, Consumer<String> consumer) {
		InterProcessMutex lock = new InterProcessMutex(zkClient, StringHelper.formatPath(LOCK_PATH + "/" + lockPath));
		try {
			if (lock.acquire(time, unit)) {
				consumer.accept(lockPath);
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				lock.release();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	/**
	 * 获取到分布式锁，并且运行代码
	 *
	 * @return true 获取到锁，并且运行了代码,false 未获取到锁
	 */
	public boolean accept(String lockPath, Consumer<String> consumer) {
		return accept(lockPath, 10, TimeUnit.SECONDS, consumer);
	}
}
