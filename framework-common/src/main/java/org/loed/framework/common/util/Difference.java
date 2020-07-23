package org.loed.framework.common.util;

import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/3/28 23:22
 */
public class Difference<T> {
	private List<T> addList;
	private List<T> updateList;
	private List<T> deleteList;

	Difference() {
	}

	public List<T> getAddList() {
		return addList;
	}

	public void setAddList(List<T> addList) {
		this.addList = addList;
	}

	public List<T> getUpdateList() {
		return updateList;
	}

	public void setUpdateList(List<T> updateList) {
		this.updateList = updateList;
	}

	public List<T> getDeleteList() {
		return deleteList;
	}

	public void setDeleteList(List<T> deleteList) {
		this.deleteList = deleteList;
	}
}
