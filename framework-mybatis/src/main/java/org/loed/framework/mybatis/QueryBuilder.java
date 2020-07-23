package org.loed.framework.mybatis;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/24 1:46 PM
 */
class QueryBuilder {
	private static final String BLANK = " ";
	//	private int setLastIndex = -1;
//	private int selectLastIndex = -1;
//	private int tablesLastIndex = -1;
//	private int whereLastIndex = -1;
//	private int orderByLastIndex = -1;
	private List<String> selectList = new ArrayList<>();
	private String table;
	private List<String> joinList = new ArrayList<>();
	private List<String> whereList = new ArrayList<>();
	private List<String> orderByList = new ArrayList<>();
	private List<String> updateList = new ArrayList<>();
	private StatementType statementType;

	private enum StatementType {
		select, update, delete
	}

	public QueryBuilder select(String... selects) {
		this.statementType = StatementType.select;
//		for (String select : selects) {
//			if (selectLastIndex < 0) {
//				sql.insert(0, "select" + BLANK);
//				selectLastIndex = ("select" + BLANK).length() - 1;
//				String selectToInsert = BLANK + select + BLANK;
//				sql.insert(selectLastIndex, selectToInsert);
//				selectLastIndex = selectLastIndex + selectToInsert.length();
//			} else {
//				String selectToInsert = BLANK + "," + select + BLANK;
//				sql.insert(selectLastIndex, selectToInsert);
//				selectLastIndex = selectLastIndex + selectToInsert.length();
//			}
//		}
		for (String select : selects) {
			selectList.add(BLANK + select + BLANK);
		}
		return this;
	}

	public QueryBuilder from(String fromClause) {
//		if (tablesLastIndex > 0) {
//			throw new RuntimeException("from clause already exists using left join or join or right join instead");
//		}
//		String fromClauseToInsert = BLANK + "from" + BLANK + fromClause + BLANK;
//		if (selectLastIndex < 0) {
//			sql.insert(0, fromClauseToInsert);
//			tablesLastIndex = fromClauseToInsert.length() - 1;
//		} else {
//			sql.insert(selectLastIndex, BLANK + "from" + BLANK + fromClause);
//			tablesLastIndex = selectLastIndex + (BLANK + "from" + BLANK + fromClause).length();
//		}
		this.table = BLANK + fromClause + BLANK;
		return this;
	}

	public QueryBuilder leftJoin(String joinClause) {
//		if (tablesLastIndex < 0) {
//			throw new RuntimeException("from clause not exists ,please set the from clause firstly");
//		}
//		String joinClauseToInsert = BLANK + "left join " + joinClause + BLANK;
//		sql.insert(tablesLastIndex, joinClauseToInsert);
//		tablesLastIndex = tablesLastIndex + joinClauseToInsert.length();
		this.joinList.add(BLANK + "left join" + BLANK + joinClause + BLANK);
		return this;
	}

	public QueryBuilder rightJoin(String joinClause) {
//		if (tablesLastIndex < 0) {
//			throw new RuntimeException("from clause not exists ,please set the from clause firstly");
//		}
//		String joinClauseToInsert = BLANK + "right join " + joinClause + BLANK;
//		sql.insert(tablesLastIndex, joinClauseToInsert);
//		tablesLastIndex = tablesLastIndex + joinClauseToInsert.length();
		this.joinList.add(BLANK + "right join" + BLANK + joinClause + BLANK);
		return this;
	}

	public QueryBuilder innerJoin(String joinClause) {
//		if (tablesLastIndex < 0) {
//			throw new RuntimeException("from clause not exists ,please set the from clause firstly");
//		}
//		String joinClauseToInsert = BLANK + "inner join " + joinClause + BLANK;
//		sql.insert(tablesLastIndex, joinClauseToInsert);
//		tablesLastIndex = tablesLastIndex + joinClauseToInsert.length();
		this.joinList.add(BLANK + "inner join" + BLANK + joinClause + BLANK);
		return this;
	}

	public QueryBuilder where(String clause) {
//		if (whereLastIndex < 0) {
//			//fist
//			sql.insert(tablesLastIndex, BLANK + "where" + BLANK);
//			whereLastIndex = tablesLastIndex + (BLANK + "where" + BLANK).length();
//			String whereToInsert = BLANK + where + BLANK;
//			sql.insert(whereLastIndex, whereToInsert);
//			whereLastIndex = whereLastIndex + whereToInsert.length();
//		} else {
//			String whereToInsert = BLANK + (joint == null ? "" : joint.name()) + BLANK + where + BLANK;
//			sql.insert(whereLastIndex, whereToInsert);
//			whereLastIndex = whereLastIndex + whereToInsert.length();
//		}
		this.whereList.add(BLANK + clause + BLANK);
		return this;
	}

	public QueryBuilder orderBy(String order) {
//		if (orderByLastIndex < 0) {
//			//fist
//			sql.insert(whereLastIndex, BLANK + "order by" + BLANK);
//			orderByLastIndex = whereLastIndex + (BLANK + "order by" + BLANK).length();
//			String orderToInsert = BLANK + order + BLANK;
//			sql.insert(orderByLastIndex, orderToInsert);
//			whereLastIndex = orderByLastIndex + orderToInsert.length();
//		} else {
//			String orderToInsert = BLANK + order + BLANK;
//			sql.insert(orderByLastIndex, orderToInsert);
//			orderByLastIndex = orderByLastIndex + orderToInsert.length();
//		}
		this.orderByList.add(BLANK + order + BLANK);
		return this;
	}

	public QueryBuilder update(String tableName) {
//		String updateToAppend = "update" + BLANK + tableName + BLANK;
//		sql.append(updateToAppend);
//		tablesLastIndex = updateToAppend.length() - 1;
		this.table = tableName;
		this.statementType = StatementType.update;
		return this;
	}

	public QueryBuilder set(String... columns) {
//		for (String column : columns) {
//			if (setLastIndex < 0) {
//				String columnToInsert = BLANK + column + BLANK;
//				sql.insert(tablesLastIndex, columnToInsert);
//				setLastIndex = tablesLastIndex + columnToInsert.length();
//			} else {
//				String columnToInsert = BLANK + "," + column + BLANK;
//				sql.insert(selectLastIndex, columnToInsert);
//				setLastIndex = setLastIndex + columnToInsert.length();
//			}
//		}
		for (String column : columns) {
			this.updateList.add(BLANK + column + BLANK);
		}
		return this;
	}

	public QueryBuilder delete(String tableName) {
//		String deleteToAppend = "delete from" + BLANK + tableName + BLANK;
//		sql.append(deleteToAppend);
//		tablesLastIndex = deleteToAppend.length() - 1;
		this.table = BLANK + tableName + BLANK;
		this.statementType = StatementType.delete;
		return this;
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
					for (String orderBy : orderByList) {
						builder.append(orderBy);
					}
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
}
