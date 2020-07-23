package org.loed.framework.common.tree;


import java.util.*;

/**
 * 树的解析类
 *
 * @author Thomason
 * @since 11-4-4 下午8:29
 */
public class TreeResolver {

	/**
	 * 将平面结构的数据，变成立体结构的数据
	 *
	 * @param trees 平面结构的树节点集合(适用于从数据库查询出来的结果)
	 * @return 立体结构的树
	 */
	public static <X extends TreeNode> List<X> resolveTree(List<X> trees) {
		return resolveTree(trees, null);
	}

	/**
	 * 将平面结构的数据，变成立体结构的数据
	 *
	 * @param trees  平面结构的树节点集合(适用于从数据库查询出来的结果)
	 * @param rootId 根节点Id
	 * @return 立体结构的树
	 */
	public static <X extends TreeNode> List<X> resolveTree(List<X> trees, String rootId) {
		if (trees == null || trees.size() == 0) {
			return trees;
		}
		Map<String, X> map = new HashMap<String, X>();
		for (X tree : trees) {
			map.put(tree.identity(), tree);
		}
		sortList(trees);
		List tree = new ArrayList();
		for (X x : trees) {
			if ((rootId == null && x.parentId() == null)
					|| (rootId != null && rootId.equals(x.identity()))) {
				tree.add(x);
			}
			String parentId = x.parentId();
			if (parentId != null) {
				X parent = map.get(parentId);
				if (parent != null) {
					parent.children().add(x);
				}
			}
		}
		return tree;
	}

	/**
	 * 解析带有选择框的树
	 *
	 * @param trees          树节点集合
	 * @param checkedNodeSet 选中节点的集合
	 * @param <X>            树节点的实现类
	 * @return 解析过的树状结构数据
	 */
	public static <X extends CheckboxTreeNode> List<X> resolveCheckboxTree(List<X> trees, Set<String> checkedNodeSet) {
		return resolveCheckboxTree(trees, null, checkedNodeSet);
	}

	/**
	 * 解析带有选择框的树
	 *
	 * @param trees          树节点集合
	 * @param rootId         根节点Id
	 * @param checkedNodeSet 选中节点的集合
	 * @param <X>            树节点的实现类
	 * @return 解析过的树状结构数据
	 */
	public static <X extends CheckboxTreeNode> List<X> resolveCheckboxTree(List<X> trees, String rootId, Set<String> checkedNodeSet) {
		if (trees == null || trees.size() == 0) {
			return trees;
		}
		for (X tree : trees) {
			if (checkedNodeSet != null && checkedNodeSet.contains(tree.identity())) {
				tree.setChecked(true);
			} else {
				tree.setChecked(false);
			}
		}
		return resolveTree(trees, rootId);
	}

	/**
	 * 对实现了树节点接口的集合进行排序
	 *
	 * @param trees 树节点集合
	 * @param <X>   实现了树节点接口的类
	 */
	private static <X extends TreeNode> void sortList(List<X> trees) {
		Collections.sort(trees, new Comparator<X>() {
			@Override
			public int compare(X o1, X o2) {
				if (o1 == null && o2 == null) {
					return 0;
				}
				if (o1 == null && o2 != null) {
					return -1;
				}
				if (o1 != null && o2 == null) {
					return 1;
				}
				if (o1.order() == null && o2.order() == null) {
					return 0;
				}
				if (o1.order() == null && o2.order() != null) {
					return -1;
				}
				if (o1.order() != null && o2.order() == null) {
					return 1;
				}
				return o1.order().compareTo(o2.order());
			}
		});

	}
}
