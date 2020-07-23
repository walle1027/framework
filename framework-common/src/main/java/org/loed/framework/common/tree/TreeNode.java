package org.loed.framework.common.tree;

import java.util.List;

/**
 * User: 杨涛
 * Date: 11-4-4
 * Time: 下午8:16
 */
public interface TreeNode {
	/**
	 * 获取树节点的子节点属性
	 *
	 * @return 树的子节点
	 */
	List children();

	/**
	 * 获取树节点的标识属性
	 *
	 * @return 树的标识
	 */
	String identity();

	/**
	 * 获取树节点的父ID属性
	 *
	 * @return 树的父节点Id
	 */
	String parentId();

	/**
	 * 获取树的排序属性
	 *
	 * @return 树的排序字段值
	 */
	Comparable order();
}
