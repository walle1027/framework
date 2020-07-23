package org.loed.framework.mybatis.sharding.table;

import org.loed.framework.common.consistenthash.ConsistentHash;
import org.loed.framework.common.consistenthash.impl.MD5Unpack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/27 下午3:33
 */
public class TableConsistentHash {

	public static Map<String, ConsistentHash<TableNode>> tableConsistentHashContainer = new ConcurrentHashMap<>();

	private static Lock lock = new ReentrantLock();

	public static ConsistentHash<TableNode> getOrCreate(String tableName, int maxCount) {
		if (tableConsistentHashContainer.containsKey(tableName)) {
			return tableConsistentHashContainer.get(tableName);
		}
		try {
			lock.lock();
			if (tableConsistentHashContainer.containsKey(tableName)) {
				return tableConsistentHashContainer.get(tableName);
			}
			ConsistentHash<TableNode> consistentHash = new ConsistentHash<>();
			consistentHash.setHashFunction(new MD5Unpack());
			consistentHash.setNumberOfReplicas(100);
			for (int i = 1; i <= maxCount; i++) {
				TableNode tableNode = new TableNode(tableName, i);
				consistentHash.add(tableNode);
			}
			tableConsistentHashContainer.put(tableName, consistentHash);
			return consistentHash;
		} finally {
			lock.unlock();
		}
	}
}
