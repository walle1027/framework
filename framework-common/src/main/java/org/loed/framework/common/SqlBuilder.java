package org.loed.framework.common;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-10-13 下午1:05
 */
public class SqlBuilder {
	public static final String AND = "and";
	public static final String OR = "or";
	public static final String SELECT = "select";
	public static final String INSERT = "insert";
	public static final String UPDATE = "update";
	public static final String DELETE = "delete";
	public static final String FROM = "from";
	public static final String WHERE = "where";
	public static final String INNER_JOIN = "inner join";
	public static final String LEFT_JOIN = "left join";
	/**
	 * sql
	 */
	private StringBuilder builder = new StringBuilder();
	/**
	 * 参数自增长的序列
	 */
	private int _param_seq;
	/**
	 * 拼接sql的时候的自增长参数序列
	 */
	private int _sql_seq;
	/**
	 * 参数列表
	 */
	private Map<String, Object> parameters = new HashMap<String, Object>();

	private Lock lock = new ReentrantLock();

	/**
	 * 默认构造方法
	 */
	public SqlBuilder() {
	}

	/**
	 * 初始化sql的构造方法
	 *
	 * @param sqlClause sql语句
	 */
	public SqlBuilder(String sqlClause) {
		this.append(sqlClause);
	}

	/**
	 * 初始化构造器的构造方法
	 *
	 * @param sqlBuilder 查询构建器
	 */
	public SqlBuilder(SqlBuilder sqlBuilder) {
		this.append(sqlBuilder);
	}

	/**
	 * 增加参数
	 *
	 * @param parameterValue 参数值
	 */
	public void parameter(Object parameterValue) {
		try {
			lock.lock();
			parameters.put(buildKey(_param_seq), parameterValue);
			_param_seq++;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 增加参数
	 *
	 * @param parameterName  参数名称
	 * @param parameterValue 参数值
	 */
	public void parameter(String parameterName, Object parameterValue) {
		try {
			lock.lock();
			parameters.put(parameterName, parameterValue);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 增加 queryBuilder
	 *
	 * @param sqlBuilder queryBuilder
	 * @return 查询构建器
	 */
	public SqlBuilder append(SqlBuilder sqlBuilder) {
		try {
			lock.lock();
			builder.append(" ");
			String sqlFragment = sqlBuilder.toString();
			for (int i = 0; i < sqlBuilder._sql_seq; i++) {
				String oldKey = ":" + buildKey(i);
				String newKey = ":" + buildKey(_sql_seq + i);
				int index = sqlFragment.indexOf(oldKey);
				builder.append(sqlFragment.substring(0, index));
				builder.append(newKey);
				sqlFragment = sqlFragment.substring(index + oldKey.length());
			}
			_sql_seq = _sql_seq + sqlBuilder._sql_seq;
			Set<String> keySet = new HashSet<String>();
			for (String s : sqlBuilder.getParameters().keySet()) {
				keySet.add(s);
			}
			for (int j = 0; j < sqlBuilder._param_seq; j++) {
				String oldKey = buildKey(j);
				String newKey = buildKey(_param_seq + j);
				if (keySet.contains(oldKey)) {
					keySet.remove(oldKey);
				}
				parameters.put(newKey, sqlBuilder.getParameters().get(oldKey));
			}
			if (!keySet.isEmpty()) {
				for (String s : keySet) {
					parameters.put(s, sqlBuilder.getParameters().get(s));
				}
			}
			_param_seq = _param_seq + sqlBuilder._param_seq;
			builder.append(sqlFragment);
			return this;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 增加sql片段
	 *
	 * @param sql sql片段
	 * @return sql构建器
	 */
	public SqlBuilder append(final String sql) {
		try {
			lock.lock();
			if (builder.length() != 0) {
				builder.append(" ");
			}
			StringTokenizer stringTokenizer = new StringTokenizer(sql, "?", true);
			while (stringTokenizer.hasMoreTokens()) {
				String fragment = stringTokenizer.nextToken();
				if (fragment.equals("?")) {
					builder.append(":").append(buildKey(_sql_seq));
					_sql_seq++;
				} else {
					builder.append(fragment);
				}
			}
			return this;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 拼装sql语句
	 *
	 * @param sql       sql片段
	 * @param parameter 参数
	 * @return sql拼装器
	 */
	public SqlBuilder append(final String sql, final Object parameter) {
		try {
			lock.lock();
			append(sql);
			parameter(parameter);
			return this;
		} finally {
			lock.unlock();
		}

	}

	public String toSqlString() {
		String sql = builder.toString();
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value != null) {
				sql = sql.replace(":" + key, "'" + value.toString() + "'");
			} else {
				sql = sql.replace(":" + key, "'null'");
			}
		}
		return sql;
	}

	/**
	 * 变为sql字符串
	 *
	 * @return sql字符串
	 */
	@Override
	public String toString() {
		return builder.toString();
	}

	/**
	 * 删除最后一个字符
	 *
	 * @return 查询构建器
	 */
	public SqlBuilder deleteLastChar() {
		try {
			lock.lock();
			this.builder.deleteCharAt(builder.length() - 1);
			return this;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 删除最后一个字符
	 *
	 * @return 查询构建器
	 */
	public SqlBuilder deleteFirstChar() {
		try {
			lock.lock();
			this.builder.deleteCharAt(0);
			return this;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 再指定位置删除字符串
	 *
	 * @param pos 位置
	 * @return 查询构建器
	 */
	public SqlBuilder deleteCharAt(int pos) {
		try {
			lock.lock();
			builder.deleteCharAt(pos);
			return this;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 取得参数列表
	 *
	 * @return 参数列表
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * 清空所有值
	 */
	public void clear() {
		builder.setLength(0);
		_param_seq = 0;
		_sql_seq = 0;
		parameters.clear();
	}

	/**
	 * 构建参数
	 *
	 * @param i 序号
	 * @return 根据序号构建参数
	 */
	private String buildKey(int i) {
		return "_p_" + i;
	}
}
