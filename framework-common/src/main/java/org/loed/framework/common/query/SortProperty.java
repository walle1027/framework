package org.loed.framework.common.query;

import java.io.Serializable;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/8/16 下午4:14
 */
public class SortProperty implements Serializable,Copyable<SortProperty> {
	private String propertyName;
	private Sort sort = Sort.ASC;

	public SortProperty() {
	}

	public SortProperty(String propertyName) {
		this.propertyName = propertyName;
	}

	public SortProperty(String propertyName, Sort sort) {
		this.propertyName = propertyName;
		this.sort = sort;
	}

	@Override
	public SortProperty copy() {
		return new SortProperty(this.propertyName,this.sort);
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}
}
