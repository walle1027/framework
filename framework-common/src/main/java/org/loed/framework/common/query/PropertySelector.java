package org.loed.framework.common.query;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 对象的属性选择器，当includes 和 excludes 同时设置时，以excludes为准;当includes和excludes都不设置时，将返回全部属性
 *
 * @author thomason
 * @version 1.0
 * @since 2018/2/5 下午5:14
 */
public class PropertySelector implements Serializable {
	/**
	 * 包含哪些属性
	 */
	private Set<String> includes;
	/**
	 * 不包含哪些属性
	 */
	private Set<String> excludes;

	public PropertySelector include(String... includes) {
		if (includes == null || includes.length == 0){
			return this;
		}
		if (excludes != null && !excludes.isEmpty()) {
			throw new RuntimeException("you can't both assign excludes and includes");
		}
		if (this.includes == null) {
			this.includes = new HashSet<>();
		}
		this.includes.addAll(Arrays.asList(includes));
		return this;
	}

	public PropertySelector exclude(String... excludes) {
		if (excludes == null || excludes.length == 0){
			return this;
		}
		if (includes != null && !includes.isEmpty()) {
			throw new RuntimeException("you can't both assign includes and excludes");
		}
		if (this.excludes == null) {
			this.excludes = new HashSet<>();
		}
		this.excludes.addAll(Arrays.asList(excludes));
		return this;
	}

	public Set<String> getIncludes() {
		return includes;
	}

	public Set<String> getExcludes() {
		return excludes;
	}
}
