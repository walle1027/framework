package org.loed.framework.common.database;

import org.junit.Test;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.database.po.User;
import org.loed.framework.jdbc.database.dialect.Dialect;
import org.loed.framework.jdbc.database.dialect.impl.MysqlDialect;

import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/8/13 上午11:21
 */
public class DialectTest {
	@Test
	public void buildCreateTableClause1() throws Exception {
		Dialect dialect = new MysqlDialect();
		Table relation = ORMapping.get(User.class);
		List<String> strings = dialect.buildCreateTableClause(relation);
		System.out.println(strings);
	}

	@Test
	public void buildAddColumnClause1() throws Exception {
		Dialect dialect = new MysqlDialect();
		Table relation = ORMapping.get(User.class);
		assert relation != null;
		relation.getColumns().forEach(column -> {
			List<String> clause = dialect.buildAddColumnClause(column);
			System.out.println(clause);
		});
	}
}
