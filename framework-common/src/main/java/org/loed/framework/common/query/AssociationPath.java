package org.loed.framework.common.query;

import org.loed.framework.common.orm.Table;

import java.io.Serializable;

/**
 * 保存一个有效路径和它的别名的类
 * 有效路径是指在hibernate中配置了关联的路径
 *
 * @author Thomason
 * @version 1.0
 * @since 11-10-11 下午11:37
 */
public class AssociationPath implements Serializable {
	/**
	 * 有效路径
	 */
	private String path;
	/**
	 *
	 */
	private Table table;
	/**
	 * 别名
	 */
	private String alias;

	public AssociationPath() {
	}

	public AssociationPath(String path) {
		this.path = path;
	}

	public AssociationPath(String path, String alias) {
		this.path = path;
		this.alias = alias;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
