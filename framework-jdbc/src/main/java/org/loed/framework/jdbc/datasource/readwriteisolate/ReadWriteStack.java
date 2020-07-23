package org.loed.framework.jdbc.datasource.readwriteisolate;

import java.io.Serializable;
import java.util.Stack;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/10/24 17:31
 */

public class ReadWriteStack implements Serializable {
	//初始的数据源类型
	private final ReadWriteStrategy initialReadWriteStrategy;
	//当前设置的数据源类型
	private ReadWriteStrategy readWriteStrategy;
	//数据源切换堆栈
	private Stack<ReadWriteStrategy> stack;

	public ReadWriteStack(ReadWriteStrategy initialReadWriteStrategy) {
		this.initialReadWriteStrategy = initialReadWriteStrategy;
		this.readWriteStrategy = initialReadWriteStrategy;
		this.stack = new Stack<>();
	}

	public void push(ReadWriteStrategy strategy) {
		stack.push(strategy);
	}

	public ReadWriteStrategy pop() {
		return stack.pop();
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public ReadWriteStrategy peek() {
		return stack.peek();
	}

	public ReadWriteStrategy getInitialReadWriteStrategy() {
		return initialReadWriteStrategy;
	}

	public ReadWriteStrategy getReadWriteStrategy() {
		return readWriteStrategy;
	}

	public void setReadWriteStrategy(ReadWriteStrategy readWriteStrategy) {
		this.readWriteStrategy = readWriteStrategy;
	}

	public Stack<ReadWriteStrategy> getStack() {
		return stack;
	}

	public void setStack(Stack<ReadWriteStrategy> stack) {
		this.stack = stack;
	}

	@Override
	public String toString() {
		return "DatasourceStack{" +
				"readWriteStrategy=" + readWriteStrategy +
				'}';
	}
}
