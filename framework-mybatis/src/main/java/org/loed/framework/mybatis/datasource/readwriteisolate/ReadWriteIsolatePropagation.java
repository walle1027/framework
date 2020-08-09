package org.loed.framework.mybatis.datasource.readwriteisolate;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/10/24 18:57
 */

public enum ReadWriteIsolatePropagation {
	//支持当前数据源，如果当前没有数据源，就新建一个设定一个。这是最常见的选择。
	required,
	//新建数据源，如果当前存在数据源，不使用当前数据源
	requiresNew
}
