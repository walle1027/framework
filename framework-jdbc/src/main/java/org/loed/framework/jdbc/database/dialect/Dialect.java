package org.loed.framework.jdbc.database.dialect;

import org.loed.framework.common.database.Column;
import org.loed.framework.common.database.Index;
import org.loed.framework.common.database.Table;

import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/8/13 上午10:20
 */
public interface Dialect {
	String BLANK = " ";

	List<String> buildCreateTableClause(Table table);

	List<String> buildAddColumnClause(Column column);

	List<String> buildUpdateColumnClause(Column column);

	List<String> buildIndexClause(Index index);
}
