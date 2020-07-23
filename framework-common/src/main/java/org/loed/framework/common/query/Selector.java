package org.loed.framework.common.query;

import java.util.HashSet;
import java.util.Set;

/**
 * 属性选择器
 *
 * @author Thomason
 * @version 1.0
 */
public class Selector {
	private Set<String> _selector;
	private static final long serialVersionUID = 1L;

	public Selector() {
		this._selector = new HashSet<>();
	}

	public Selector(int initialCapacity) {
		this._selector = new HashSet<>(initialCapacity);
	}

	public static Selector create() {
		return new Selector();
	}

	public Selector add(String property) {
		if (property.startsWith(".")) {
			this._selector.add(property);
		} else {
			this._selector.add("." + property);
		}
		return this;
	}

	/**
	 * 根据属性，生成子选择器
	 *
	 * @param property 属性
	 * @return 新的选择器
	 */
	public Selector generateSubSelector(String property) {
		Selector subSelector = new Selector();
		for (String s : this._selector) {
			if (s.contains(property)) {
				subSelector.add(s.substring(property.length() + 1, s.length()));
			}
		}
		return subSelector;
	}

	/**
	 * 判断选择器中是否包含属性
	 *
	 * @param property 属性
	 * @return 是否包含
	 */
	public boolean containsProperty(String property) {
		for (String s : this._selector) {
			if (s.contains("." + property)) {
				return true;
			}
		}
		return false;
	}

	public boolean contains(String prop){
		return this._selector.contains(prop);
	}

}
