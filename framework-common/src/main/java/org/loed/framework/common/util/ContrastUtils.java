package org.loed.framework.common.util;

import org.apache.commons.collections4.CollectionUtils;
import org.loed.framework.common.po.Identify;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-10-25 下午5:41
 */

public class ContrastUtils {
	public static <T> Collection<T> join(Collection<T> a, Collection<T> b, Comparator<T> comparator) {
		List<T> result = new ArrayList<>();
		for (T t : a) {
			for (T t1 : b) {
				if (comparator.compare(t, t1) == 0) {
					result.add(t1);
				}
			}
		}
		return result;
	}

	/**
	 * 过滤集合中的空元素
	 *
	 * @param coll 集合
	 * @param <T>
	 * @return
	 */
	public static <T> void filterEmpty(Collection<T> coll) {
		if (CollectionUtils.isNotEmpty(coll)) {
			coll = coll.stream().filter(Objects::nonNull).collect(Collectors.toList());
			/*CollectionUtils.filter(coll, new Predicate() {
				@Override
				public boolean evaluate(Object object) {
					return object != null;
				}
			});*/
		}
	}

	/**
	 * 比较原来的VOList和POList的差异
	 *
	 * @param voList voList
	 * @param poList poList
	 * @return 比较后的结果
	 */
	public static <T extends Identify> Difference<T> contrast(List<T> voList, List<T> poList) {
		Difference<T> difference = new Difference<T>();
		List<T> _voList = new LinkedList<T>();
		List<T> _poList = new LinkedList<T>();
		if (CollectionUtils.isNotEmpty(voList)) {
			_voList.addAll(voList);
		}
		if (CollectionUtils.isNotEmpty(poList)) {
			_poList.addAll(poList);
		}
		List<T> updateList = new ArrayList<T>();
		List<T> deleteList = new ArrayList<T>();
		List<T> addList = new ArrayList<T>();
		for (Iterator<T> i = _poList.iterator(); i.hasNext(); ) {
			T poItem = i.next();
			boolean isDeletedItem = true;
			for (Iterator<T> j = _voList.iterator(); j.hasNext(); ) {
				T voItem = j.next();
				if (Objects.equals(poItem.getId(),voItem.getId())) {
					updateList.add(voItem);
					isDeletedItem = false;
					j.remove();
				}
			}
			if (isDeletedItem) {
				i.remove();
				deleteList.add(poItem);
			}
		}
		//新增
		if (_voList.size() > 0) {
			for (T voItem : _voList) {
				addList.add(voItem);
			}
		}
		difference.setAddList(addList);
		difference.setUpdateList(updateList);
		difference.setDeleteList(deleteList);
		return difference;
	}

	/**
	 * 比较原来的VOList和POList的差异
	 *
	 * @param voList voList
	 * @param poList poList
	 * @return 比较后的结果
	 */
	public static <T> Difference<T> contrast(List<T> voList, List<T> poList, Comparator<T> comparator) {
		Difference<T> difference = new Difference<T>();
		List<T> _voList = new LinkedList<T>();
		List<T> _poList = new LinkedList<T>();
		if (CollectionUtils.isNotEmpty(voList)) {
			_voList.addAll(voList);
		}
		if (CollectionUtils.isNotEmpty(poList)) {
			_poList.addAll(poList);
		}
		List<T> updateList = new ArrayList<T>();
		List<T> deleteList = new ArrayList<T>();
		List<T> createList = new ArrayList<T>();
		for (Iterator<T> i = _poList.iterator(); i.hasNext(); ) {
			T poItem = i.next();
			boolean isDeletedItem = true;
			for (Iterator<T> j = _voList.iterator(); j.hasNext(); ) {
				T voItem = j.next();
				if (comparator.compare(voItem, poItem) == 0) {
					updateList.add(voItem);
					isDeletedItem = false;
					j.remove();
				}
			}
			if (isDeletedItem) {
				i.remove();
				deleteList.add(poItem);
			}
		}
		//新增
		if (_voList.size() > 0) {
			for (T voItem : _voList) {
				createList.add(voItem);
			}
		}
		difference.setAddList(createList);
		difference.setUpdateList(updateList);
		difference.setDeleteList(deleteList);
		return difference;
	}
}
