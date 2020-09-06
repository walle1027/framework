package org.loed.framework.common.orm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/6/4 上午11:58
 */
public class JoinTable {
	/**
	 * 表之间的级联关系
	 */
	private Relation relation;
	/**
	 * 对象的级联映射属性
	 */
	private String fieldName;
	/**
	 * 目标表名称
	 */
	private String targetTableName;
	/**
	 * 关联的列
	 */
	private List<JoinColumn> joinColumns;
	/**
	 * 关联的对象实体
	 */
	private Class<?> targetEntity;

	public JoinTable(Relation relation, String fieldName, Class targetEntity) {
		this.relation = relation;
		this.fieldName = fieldName;
		this.targetEntity = targetEntity;
	}

	public void addJoinColumn(JoinColumn joinColumn) {
		if (this.joinColumns == null) {
			this.joinColumns = new ArrayList<>();
		}
		this.joinColumns.add(joinColumn);
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	public String getTargetTableName() {
		return targetTableName;
	}

	public void setTargetTableName(String targetTableName) {
		this.targetTableName = targetTableName;
	}

	public List<JoinColumn> getJoinColumns() {
		return joinColumns;
	}

	public void setJoinColumns(List<JoinColumn> joinColumns) {
		this.joinColumns = joinColumns;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Class getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(Class targetEntity) {
		this.targetEntity = targetEntity;
	}
}
