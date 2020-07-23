package org.loed.framework.common.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/7/11 20:47
 */
public class ZKHolder {
	private static Logger logger = LoggerFactory.getLogger(ZKHolder.class);
	private static Lock lock = new ReentrantLock();
	private static Map<String, CuratorFramework> zkClientMap = new ConcurrentHashMap<>();

	public static CuratorFramework get(String address) {
		if (zkClientMap.get(address) != null) {
			return zkClientMap.get(address);
		}
		try {
			lock.lock();
			if (zkClientMap.get(address) != null) {
				return zkClientMap.get(address);
			}
			CuratorFramework zkClient = CuratorFrameworkFactory.newClient(address, new RetryNTimes(10, 5000));
			zkClient.start();
			zkClientMap.put(address, zkClient);
			return zkClient;
		} finally {
			lock.unlock();
		}
	}

	public static void close(String address) {
		if (zkClientMap.containsKey(address)) {
			CuratorFramework zkClient = zkClientMap.get(address);
			try {
				zkClient.close();
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
			zkClientMap.remove(address);
		}
	}

	public static void checkOrCreatePath(CuratorFramework zkClient, String path) {
		try {
			if (zkClient.checkExists().forPath(path) == null) {
				zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
