package org.loed.framework.mybatis;

public enum BatchType {
	None,
	BatchInsert,
	BatchUpdateNonBlank,
	BatchUpdateNonNull,
	BatchUpdateFixed
}
