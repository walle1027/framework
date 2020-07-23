package org.loed.framework.common.sequence.suppport;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.loed.framework.common.lock.ZKDistributeLock;
import org.loed.framework.common.sequence.spi.SequencePersister;
import org.loed.framework.common.util.ByteUtils;
import org.loed.framework.common.util.StringHelper;
import org.loed.framework.common.zookeeper.ZKHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/3/21.21:17
 */
public class ZKSequenceSequencePersister implements SequencePersister {
	private static final String ZK_SEQUENCE_PATH = "/sequence";
	private final String zkAddress;
	private Logger logger = LoggerFactory.getLogger(ZKSequenceSequencePersister.class);
	private ZKDistributeLock distributeLock;

	public ZKSequenceSequencePersister(String zkAddress) {
		this.zkAddress = zkAddress;
	}

	@Override
	public Long getNext(String sequenceName, int step) {
		CuratorFramework curator = ZKHolder.get(zkAddress);
		String path = buildPath(sequenceName);
		if (distributeLock != null) {
			AtomicLong seq = new AtomicLong(0);
			distributeLock.accept(path, (p) -> {
				seq.set(getSequence(step, curator, path));
			});
			return seq.get();
		} else {
			return getSequence(step, curator, path);
		}
	}

	private long getSequence(int step, CuratorFramework curator, String path) {
		try {
			boolean exists = curator.checkExists().forPath(path) != null;
			if (exists) {
				byte[] bytes = curator.getData().forPath(path);
				long seq = ByteUtils.toLong(bytes);
				curator.setData().forPath(path, ByteUtils.fromLong(seq + step));
				return seq + step;
			} else {
				curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, ByteUtils.fromLong(step));
				return (long) step;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return (long) step;
	}

	@Override
	public void setOffset(String sequenceName, long offset) {
		CuratorFramework curator = ZKHolder.get(zkAddress);
		String path = buildPath(sequenceName);
		if (distributeLock != null) {
			distributeLock.accept(path, p -> {
				setOffset(curator, path, offset);
			});
		} else {
			setOffset(curator, path, offset);
		}

	}

	private void setOffset(CuratorFramework curator, String path, long offset) {
		try {
			boolean exists = curator.checkExists().forPath(path) != null;
			if (exists) {
				curator.setData().forPath(path, ByteUtils.fromLong(offset));
			} else {
				curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, ByteUtils.fromLong(offset));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private String buildPath(String sequenceName) {
		return StringHelper.formatPath(ZK_SEQUENCE_PATH + "/" + sequenceName);
	}

	public void setDistributeLock(ZKDistributeLock distributeLock) {
		this.distributeLock = distributeLock;
	}
}
