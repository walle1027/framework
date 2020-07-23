package org.loed.framework.mybatis;

public enum BatchType {
	None,
	BatchInsert,
	BatchUpdateSelective,
	BatchUpdate,
	BatchGetList,
	BatchGetByIdList,
}
