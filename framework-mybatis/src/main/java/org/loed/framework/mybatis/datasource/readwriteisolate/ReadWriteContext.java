package org.loed.framework.mybatis.datasource.readwriteisolate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/22 22:58
 */
public class ReadWriteContext {
	private static final ThreadLocal<ReadWriteStack> rw = new ThreadLocal<ReadWriteStack>();
	private static Logger logger = LoggerFactory.getLogger(ReadWriteContext.class);

	/**
	 * 标记当前线程使用读数据源
	 */
	public static void markAsRead(ReadWriteIsolatePropagation propagation) {
		ReadWriteStack readWriteStack = rw.get();
		if (readWriteStack == null) {
			readWriteStack = new ReadWriteStack(ReadWriteStrategy.read);
		}
		if (ReadWriteIsolatePropagation.required.equals(propagation)) {
			readWriteStack.setReadWriteStrategy(readWriteStack.getReadWriteStrategy());
		} else if (ReadWriteIsolatePropagation.requiresNew.equals(propagation)) {
			readWriteStack.setReadWriteStrategy(ReadWriteStrategy.read);
		}
		readWriteStack.push(ReadWriteStrategy.read);
		if (logger.isDebugEnabled()) {
			logger.debug(readWriteStack.toString());
		}
		rw.set(readWriteStack);
	}

	/**
	 * 标记当前线程使用写数据源
	 */
	public static void markAsWrite(ReadWriteIsolatePropagation propagation) {
		ReadWriteStack readWriteStack = rw.get();
		if (readWriteStack == null) {
			readWriteStack = new ReadWriteStack(ReadWriteStrategy.write);
		}
		if (ReadWriteIsolatePropagation.required.equals(propagation)) {
			readWriteStack.setReadWriteStrategy(readWriteStack.getReadWriteStrategy());
		} else if (ReadWriteIsolatePropagation.requiresNew.equals(propagation)) {
			readWriteStack.setReadWriteStrategy(ReadWriteStrategy.write);
		}
		readWriteStack.push(ReadWriteStrategy.write);
		if (logger.isDebugEnabled()) {
			logger.debug(readWriteStack.toString());
		}
		rw.set(readWriteStack);
	}


	/**
	 * 获取当前的数据源类型
	 *
	 * @return 数据源类型
	 */
	public static ReadWriteStrategy getRwType() {
		ReadWriteStack readWriteStack = rw.get();
		if (readWriteStack != null) {
			return readWriteStack.getReadWriteStrategy();
		}
		return null;
	}

	public static void clean(ReadWriteIsolatePropagation propagation) {
		ReadWriteStack readWriteStack = rw.get();
		if (readWriteStack == null) {
			return;
		}
		if (readWriteStack.isEmpty()) {
			rw.remove();
			return;
		}
		readWriteStack.pop();
		if (readWriteStack.isEmpty()) {
			rw.remove();
		} else {
			ReadWriteStrategy peek = readWriteStack.peek();
			if (ReadWriteIsolatePropagation.required.equals(propagation)) {
				readWriteStack.setReadWriteStrategy(readWriteStack.getInitialReadWriteStrategy());
			} else if (ReadWriteIsolatePropagation.requiresNew.equals(propagation)) {
				readWriteStack.setReadWriteStrategy(peek);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(readWriteStack.toString());
			}
			rw.set(readWriteStack);
		}
	}
}
