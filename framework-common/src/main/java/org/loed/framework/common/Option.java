package org.loed.framework.common;

import java.io.Serializable;

/**
 * 下拉单的类
 *
 * @author 杨涛
 * @version 1.0
 */
public class Option implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 下了单的值
	 */
	private String value;
	/**
	 * 下拉单的文本
	 */
	private String text;

	/**
	 * 下拉单是否选中
	 */
	private boolean selected;
	//该选项是否为默认值
	private boolean isDefault;

	public Option() {
		super();
	}

	public Option(String value, String text) {
		super();
		this.text = text;
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Option other = (Option) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean aDefault) {
		isDefault = aDefault;
	}
}
