package org.loed.framework.common.query;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/24 1:46 PM
 */
@NotThreadSafe
public class QueryBuilder {
	private static final String BLANK = " ";
	private final List<String> selectList = new ArrayList<>();
	private String table;
	private final List<String> joinList = new ArrayList<>();
	private final List<String> whereList = new ArrayList<>();
	private final List<String> orderByList = new ArrayList<>();
	private final List<String> updateList = new ArrayList<>();
	private StatementType statementType;

	public enum StatementType {
		/**
		 * 删除语句
		 */
		delete,
		/**
		 * 查询语句
		 */
		select,
		/**
		 * 更新语句
		 */
		update
	}

	public QueryBuilder select(String... selects) {
		this.statementType = StatementType.select;
		for (String select : selects) {
			selectList.add(BLANK + select + BLANK);
		}
		return this;
	}

	public QueryBuilder from(String fromClause) {
		this.table = BLANK + fromClause + BLANK;
		return this;
	}

	public QueryBuilder leftJoin(String joinClause) {
		this.joinList.add(BLANK + "left join" + BLANK + joinClause + BLANK);
		return this;
	}

	public QueryBuilder rightJoin(String joinClause) {
		this.joinList.add(BLANK + "right join" + BLANK + joinClause + BLANK);
		return this;
	}

	public QueryBuilder innerJoin(String joinClause) {
		this.joinList.add(BLANK + "inner join" + BLANK + joinClause + BLANK);
		return this;
	}

	public QueryBuilder where(String clause) {
		this.whereList.add(BLANK + clause + BLANK);
		return this;
	}

	public QueryBuilder orderBy(String order) {
		this.orderByList.add(BLANK + order + BLANK);
		return this;
	}

	public QueryBuilder update(String tableName) {
		this.table = tableName;
		this.statementType = StatementType.update;
		return this;
	}

	public QueryBuilder set(String... columns) {
		for (String column : columns) {
			this.updateList.add(BLANK + column + BLANK);
		}
		return this;
	}

	public QueryBuilder delete(String tableName) {
		this.table = BLANK + tableName + BLANK;
		this.statementType = StatementType.delete;
		return this;
	}

	public List<String> getUpdateList() {
		return updateList;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		switch (statementType) {
			case delete:
				builder.append("delete from").append(BLANK).append(table).append(BLANK);
				buildWhereClause(builder);
				break;
			case select:
				builder.append("select").append(BLANK);
				for (int i = 0; i < selectList.size(); i++) {
					String select = selectList.get(i);
					if (i > 0) {
						builder.append(",");
					}
					builder.append(select);
				}
				builder.append(BLANK).append("from").append(BLANK).append(table).append(BLANK);
				if (CollectionUtils.isNotEmpty(joinList)) {
					for (String join : joinList) {
						builder.append(join);
					}
				}
				buildWhereClause(builder);
				if (CollectionUtils.isNotEmpty(orderByList)) {
					builder.append(BLANK).append("order by").append(BLANK);
					builder.append(String.join(",", orderByList));
				}
				break;
			case update:
				builder.append("update").append(BLANK).append(table).append(BLANK).append("set").append(BLANK);
				for (int i = 0; i < updateList.size(); i++) {
					String update = updateList.get(i);
					if (i > 0) {
						builder.append(",");
					}
					builder.append(update);
				}
				buildWhereClause(builder);
			default:
				break;
		}

		return builder.toString();
	}

	private void buildWhereClause(StringBuilder sql) {
		if (CollectionUtils.isNotEmpty(whereList)) {
			sql.append(BLANK).append("where").append(BLANK);
			for (int i = 0; i < whereList.size(); i++) {
				String where = whereList.get(i);
				if (i == 0) {
					String whereToInsert = where.trim();
					if (StringUtils.startsWithIgnoreCase(whereToInsert, "and")) {
						sql.append(BLANK).append(whereToInsert.substring(3));
					} else if (StringUtils.startsWithIgnoreCase(whereToInsert, "or")) {
						sql.append(BLANK).append(whereToInsert.substring(2));
					} else {
						sql.append(BLANK).append(where);
					}
				} else {
					String previous = whereList.get(i - 1);
					String previousTrim = previous.trim();
					if (StringUtils.endsWithIgnoreCase(previousTrim, "(") || StringUtils.endsWithIgnoreCase(previousTrim, "where")) {
						//this case need remove prefix;
						String whereToTrim = where.trim();
						if (StringUtils.startsWithIgnoreCase(whereToTrim, "and")) {
							sql.append(BLANK).append(whereToTrim.substring(3));
						} else if (StringUtils.startsWithIgnoreCase(whereToTrim, "or")) {
							sql.append(BLANK).append(whereToTrim.substring(2));
						} else if (StringUtils.equalsIgnoreCase(whereToTrim, ")")) {
							sql.append(BLANK).append("1=1").append(BLANK).append(where);
						} else {
							sql.append(BLANK).append(where);
						}
					} else {
						sql.append(BLANK).append(where);
					}
				}
			}
		}
	}

	public StatementType getStatementType() {
		return statementType;
	}
}
