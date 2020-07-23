package org.loed.framework.common.tree;

/**
 * @author Thomason
 * @version 1.0
 * @since 12-5-5 上午12:23
 */

public interface CheckboxTreeNode extends TreeNode {
	/**
	 * 设置该节点的选择状态
	 *
	 * @param checked 选择状态 true 选中 false 未选中
	 */
	void setChecked(boolean checked);
}
