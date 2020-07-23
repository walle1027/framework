package org.loed.framework.mybatis.sharding.table;


import org.loed.framework.common.consistenthash.ConsistentHashNode;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/27 下午3:33
 */
public class TableNode implements ConsistentHashNode {

	private String tableName;
	private int index;

	public TableNode(String tableName, int index) {
		this.tableName = tableName;
		this.index = index;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String hashString() {
		return "" + index;
	}
}
