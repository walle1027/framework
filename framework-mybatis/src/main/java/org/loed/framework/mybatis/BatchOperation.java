package org.loed.framework.mybatis;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.loed.framework.common.database.Column;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.util.DataType;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.common.util.StringHelper;

import javax.persistence.GenerationType;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.loed.framework.mybatis.MybatisSqlBuilder.BLANK;


/**
 * @author thomason
 */
@SuppressWarnings("Duplicates")
public interface BatchOperation {
	SimpleDateFormat ddf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	SimpleDateFormat tsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

	default List<List> sliceBatch(List poList, int batchSize) {
		int size = poList.size();
		int numOfBatch = size / batchSize + 1;
		List<List> batches = new ArrayList<>(numOfBatch);
		int currentEndIndex = 0;
		for (int i = 0; i < numOfBatch; i++) {
			currentEndIndex = currentEndIndex + batchSize;
			if (size <= currentEndIndex) {
				batches.add(poList.subList(i * batchSize, size));
				break;
			} else {
				batches.add(poList.subList(i * batchSize, currentEndIndex));
			}
		}
		return batches;
	}

	default BatchType getBatchType(Invocation invocation) {
		final Object[] args = invocation.getArgs();
		final MappedStatement ms = (MappedStatement) args[0];
		SqlSource sqlSource = ms.getSqlSource();
		BoundSql boundSql = sqlSource.getBoundSql(args[1]);
		String sql = boundSql.getSql();
		MappedStatement mappedStatement = MybatisUtils.copyFromNewSql(ms, boundSql, sql);
		args[0] = mappedStatement;
		try {
			return BatchType.valueOf(sql);
		} catch (IllegalArgumentException ex) {
			return BatchType.None;
		}
	}

	default int doOneBatchInsert(Connection conn, List poList, Table table, String sql) throws SQLException {
		List<Column> insertList = table.getColumns().stream().filter(Column::isInsertable).collect(Collectors.toList());
		Column pkColumn = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		boolean autoGenerateId = false;
		if (pkColumn != null && GenerationType.AUTO.equals(pkColumn.getIdGenerationType())) {
			autoGenerateId = true;
		}
		int[] resultArr = null;
		PreparedStatement ps = null;
		if (autoGenerateId) {
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		} else {
			ps = conn.prepareStatement(sql);
		}
		try {
			for (Object po : poList) {
				preparePoForInsert(ps, po, insertList);
			}
			resultArr = ps.executeBatch();
			if (autoGenerateId) {
				ResultSet generatedKeys = ps.getGeneratedKeys();
				int index = 0;
				while (generatedKeys.next()) {
					ReflectionUtils.setFieldValue(poList.get(index), pkColumn.getJavaName(), generatedKeys.getLong(1));
					index++;
				}
			}
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception ignored) {
				}
			}
		}
		if (resultArr != null) {
			int sum = 0;
			for (int i : resultArr) {
				sum = sum + i;
			}
			return sum;
		} else {
			return 0;
		}
	}

	default void preparePoForInsert(PreparedStatement ps, Object po, List<Column> columns) throws SQLException {
		for (int i = 0; i < columns.size(); i++) {
			Column column = columns.get(i);
			//处理 boolean型
			int dataType = DataType.getDataType(column.getJavaType());
			if ((DataType.DT_Boolean == dataType || DataType.DT_boolean == dataType)
					&& JDBCType.INTEGER.getVendorTypeNumber() == column.getSqlType()) {
				Object value = ReflectionUtils.getFieldValue(po, column.getJavaName());
				Boolean booleanValue = (Boolean) DataType.toType(value, DataType.DT_Boolean);
				if (booleanValue != null) {
					ps.setObject(i + 1, booleanValue ? 1 : 0, column.getSqlType());
				} else {
					ps.setObject(i + 1, null, column.getSqlType());
				}
			} else {
				ps.setObject(i + 1, ReflectionUtils.getFieldValue(po, column.getJavaName()), column.getSqlType());
			}
		}
		ps.addBatch();
	}

	default String buildUpdateSelective(List<Column> columns, StringBuilder builder, String tableSqlName,
	                                    Object po, Table table, Set<String> includeColumns) {
		boolean hasInclude = CollectionUtils.isNotEmpty(includeColumns);
		builder.append("update")
				.append(BLANK)
				.append(tableSqlName)
				.append(BLANK)
				.append("set").append(BLANK);
		if (table.hasVersionColumn()) {
			Column versionColumn = table.getVersionColumn();
			builder.append(versionColumn.getSqlName()).append(BLANK).append("=").append(BLANK)
					.append(versionColumn.getSqlName()).append(" + 1 ,");
		}
		for (Column column : columns) {
			if (!column.isUpdatable()) {
				continue;
			}
			Object fieldValue = ReflectionUtils.getFieldValue(po, column.getJavaName());
			boolean include = false;
			if (hasInclude && includeColumns.contains(column.getJavaName())) {
				include = true;
			} else if (fieldValue != null) {
				include = true;
			}
			if (include) {
				builder.append(BLANK);
				builder.append(column.getSqlName()).append("=").append(getSqlForVal(fieldValue, column)).append(",");
			}
		}
		builder.deleteCharAt(builder.length() - 1);
		builder.append(BLANK).append("where").append(BLANK);

		AtomicInteger pkIndex = new AtomicInteger(0);
		table.getColumns().stream().filter(Column::isPk).forEach(column -> {
			if (pkIndex.get() > 0) {
				builder.append(BLANK).append("and").append(BLANK);
			}
			Object fieldValue = ReflectionUtils.getFieldValue(po, column.getJavaName());
			builder.append(column.getSqlName()).append("='").append(fieldValue).append("'").append(BLANK);
			pkIndex.getAndIncrement();
		});
		return builder.toString();
	}

	default String buildUpdate(List<Column> columns, StringBuilder builder, String tableSqlName,
	                           Object po, Table table, Set<String> includeColumns) {
		boolean hasInclude = CollectionUtils.isNotEmpty(includeColumns);
		builder.append("update")
				.append(BLANK)
				.append(tableSqlName)
				.append(BLANK)
				.append("set").append(BLANK);
		if (table.hasVersionColumn()) {
			Column versionColumn = table.getVersionColumn();
			builder.append(versionColumn.getSqlName()).append(BLANK).append("=").append(BLANK)
					.append(versionColumn.getSqlName()).append(" + 1 ,");
		}
		for (Column column : columns) {
			if (!column.isUpdatable()) {
				continue;
			}
			boolean include = false;
			if (hasInclude) {
				if (includeColumns.contains(column.getJavaName())) {
					include = true;
				}
			} else {
				include = true;
			}
			if (include) {
				Object fieldValue = ReflectionUtils.getFieldValue(po, column.getJavaName());
				builder.append(BLANK);
				builder.append(column.getSqlName()).append("=").append(getSqlForVal(fieldValue, column)).append(",");
			}
		}
		builder.deleteCharAt(builder.length() - 1);
		builder.append(BLANK).append("where").append(BLANK);

		AtomicInteger pkIndex = new AtomicInteger(0);
		table.getColumns().stream().filter(Column::isPk).forEach(column -> {
			if (pkIndex.get() > 0) {
				builder.append(BLANK).append("and").append(BLANK);
			}
			Object fieldValue = ReflectionUtils.getFieldValue(po, column.getJavaName());
			builder.append(column.getSqlName()).append("='").append(fieldValue).append("'").append(BLANK);
			pkIndex.getAndIncrement();
		});
		return builder.toString();
	}

	default String buildInsertSql(String tableSqlName, List<Column> columns, StringBuilder builder) {
		builder.append("insert into");
		builder.append(BLANK);
		builder.append(tableSqlName);
		builder.append(BLANK).append("(");
		for (Column column : columns) {
			if (column.isInsertable()) {
				builder.append(BLANK).append(column.getSqlName()).append(",");
			}
		}
		builder.deleteCharAt(builder.length() - 1);
		builder.append(")").append(BLANK).append("values").append(BLANK).append("(");
		for (Column column : columns) {
			if (column.isInsertable()) {
				builder.append(BLANK).append("?").append(",");
			}
		}
		builder.deleteCharAt(builder.length() - 1).append(")");
		return builder.toString();
	}

	default String getSqlForVal(Object val, Column column) {
		if (val == null) {
			return "null";
		}
		return "'" + StringHelper.escapeSql(getSqlForNotNullValue(val, column)) + "'";
	}

	default String getSqlForNotNullValue(Object val, Column column) {
		try {
			switch (column.getSqlType()) {
				case Types.BOOLEAN:

					if (val instanceof Boolean) {
						return getBoolean((Boolean) val);

					} else if (val instanceof String) {
						return getBoolean("true".equalsIgnoreCase((String) val) || !"0".equalsIgnoreCase((String) val));

					} else if (val instanceof Number) {
						int intValue = ((Number) val).intValue();

						return getBoolean(intValue != 0);

					} else {
						throw new RuntimeException("No conversion from " + val.getClass().getName() + " to Types.BOOLEAN possible.");
					}

				case Types.BIT:
				case Types.TINYINT:
				case Types.SMALLINT:
				case Types.INTEGER:
				case Types.BIGINT:
				case Types.REAL:
				case Types.FLOAT:
				case Types.DOUBLE:
				case Types.DECIMAL:
				case Types.NUMERIC:
					int scale = 0;
					if (val instanceof BigDecimal) {
						scale = ((BigDecimal) val).scale();
					}
					return getNumericObject(val, column.getSqlType(), scale);


				case Types.CHAR:
				case Types.VARCHAR:
				case Types.LONGVARCHAR:
					if (val instanceof BigDecimal) {
						return ((StringHelper.fixDecimalExponent(StringHelper.consistentToString((BigDecimal) val))));
					} else {
						return (val.toString());
					}


				case Types.BINARY:
				case Types.VARBINARY:
				case Types.LONGVARBINARY:
				case Types.BLOB:
				case Types.CLOB:
					throw new RuntimeException("No conversion from " + val.getClass().getName() + " to BINARY, VARBINARY, LONGVARBINARY, BLOB, CLOB .");


				case Types.DATE:
				case Types.TIMESTAMP:

					java.util.Date parameterAsDate;

					if (val instanceof String) {
						ParsePosition pp = new ParsePosition(0);
						java.text.DateFormat sdf = new SimpleDateFormat(getDateTimePattern((String) val, false), Locale.US);
						parameterAsDate = sdf.parse((String) val, pp);

					} else {
						parameterAsDate = (java.util.Date) val;
					}

					switch (column.getSqlType()) {
						case Types.DATE:
							if (parameterAsDate instanceof java.sql.Date) {
								return ddf.format(parameterAsDate);
							} else {
								return ddf.format(new java.sql.Date(parameterAsDate.getTime()));
							}
						case Types.TIMESTAMP:
							SimpleDateFormat tsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
							if (parameterAsDate instanceof Timestamp) {
								return tsdf.format((Timestamp) parameterAsDate) + ".0";
							} else {
								return tsdf.format(new Timestamp(parameterAsDate.getTime())) + ".0";
							}
						default:
							break;

					}

					break;

				case Types.TIME:

					if (val instanceof String) {
						java.text.DateFormat sdf = new SimpleDateFormat(getDateTimePattern((String) val, true), Locale.US);

						return (new Time(sdf.parse((String) val).getTime())).toString();

					} else if (val instanceof Timestamp) {
						Timestamp xT = (Timestamp) val;
						return (new Time(xT.getTime())).toString();
					} else {
						return val.toString();
					}

				default:
					throw new RuntimeException("type not supported" + val.getClass().getName());
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		throw new RuntimeException("invalid column for getSqlValue " + val.getClass().getName());
	}

	default String getNumericObject(Object val, int sqlType, int scale) {
		Number parameterAsNum;
		if (val instanceof Boolean) {
			return (Boolean) val ? "1" : "0";
		} else if (val instanceof String) {
			switch (sqlType) {
				case Types.BIT:
					if ("1".equals(val) || "0".equals(val)) {
						parameterAsNum = Integer.valueOf((String) val);
					} else {
						boolean parameterAsBoolean = "true".equalsIgnoreCase((String) val);

						parameterAsNum = parameterAsBoolean ? Integer.valueOf(1) : Integer.valueOf(0);
					}

					break;

				case Types.TINYINT:
				case Types.SMALLINT:
				case Types.INTEGER:
					parameterAsNum = Integer.valueOf((String) val);

					break;

				case Types.BIGINT:
					parameterAsNum = Long.valueOf((String) val);

					break;

				case Types.REAL:
					parameterAsNum = Float.valueOf((String) val);

					break;

				case Types.FLOAT:
				case Types.DOUBLE:
					parameterAsNum = Double.valueOf((String) val);

					break;

				case Types.DECIMAL:
				case Types.NUMERIC:
				default:
					parameterAsNum = new BigDecimal((String) val);
			}
		} else {
			parameterAsNum = (Number) val;
		}

		switch (sqlType) {
			case Types.BIT:
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
				return String.valueOf(parameterAsNum.intValue());


			case Types.BIGINT:
				return String.valueOf(parameterAsNum.longValue());


			case Types.REAL:
				return StringHelper.fixDecimalExponent(String.valueOf(parameterAsNum.floatValue()));


			case Types.FLOAT:
			case Types.DOUBLE:
				return StringHelper.fixDecimalExponent(String.valueOf(parameterAsNum.doubleValue()));


			case Types.DECIMAL:
			case Types.NUMERIC:

				if (parameterAsNum instanceof BigDecimal) {
					BigDecimal scaledBigDecimal = null;

					try {
						scaledBigDecimal = ((BigDecimal) parameterAsNum).setScale(scale);
					} catch (ArithmeticException ex) {
						try {
							scaledBigDecimal = ((BigDecimal) parameterAsNum).setScale(scale, BigDecimal.ROUND_HALF_UP);
						} catch (ArithmeticException arEx) {
							throw new RuntimeException("Can't set scale of '" + scale + "' for DECIMAL argument '" + parameterAsNum + "'");
						}
					}

					return StringHelper.fixDecimalExponent(StringHelper.consistentToString(scaledBigDecimal));
				} else if (parameterAsNum instanceof java.math.BigInteger) {
					return StringHelper.fixDecimalExponent(StringHelper.consistentToString(new BigDecimal((java.math.BigInteger) parameterAsNum, scale)));
				} else {
					return StringHelper.fixDecimalExponent(StringHelper.consistentToString(new BigDecimal(parameterAsNum.doubleValue())));
				}
		}
		throw new RuntimeException("convert to number fail " + val.getClass().getName() + "   " + val.toString());
	}

	default String getBoolean(boolean b) {
		return b ? "1" : "0";
	}

	default String getDateTimePattern(String dt, boolean toTime) throws Exception {
		//
		// Special case
		//
		int dtLength = (dt != null) ? dt.length() : 0;

		if ((dtLength >= 8) && (dtLength <= 10)) {
			int dashCount = 0;
			boolean isDateOnly = true;

			for (int i = 0; i < dtLength; i++) {
				char c = dt.charAt(i);

				if (!Character.isDigit(c) && (c != '-')) {
					isDateOnly = false;

					break;
				}

				if (c == '-') {
					dashCount++;
				}
			}

			if (isDateOnly && (dashCount == 2)) {
				return "yyyy-MM-dd";
			}
		}

		//
		// Special case - time-only
		//
		boolean colonsOnly = true;

		for (int i = 0; i < dtLength; i++) {
			char c = dt.charAt(i);

			if (!Character.isDigit(c) && (c != ':')) {
				colonsOnly = false;

				break;
			}
		}

		if (colonsOnly) {
			return "HH:mm:ss";
		}

		int n;
		int z;
		int count;
		int maxvecs;
		char c;
		char separator;
		StringReader reader = new StringReader(dt + " ");
		ArrayList<Object[]> vec = new ArrayList<Object[]>();
		ArrayList<Object[]> vecRemovelist = new ArrayList<Object[]>();
		Object[] nv = new Object[3];
		Object[] v;
		nv[0] = Character.valueOf('y');
		nv[1] = new StringBuilder();
		nv[2] = Integer.valueOf(0);
		vec.add(nv);

		if (toTime) {
			nv = new Object[3];
			nv[0] = Character.valueOf('h');
			nv[1] = new StringBuilder();
			nv[2] = Integer.valueOf(0);
			vec.add(nv);
		}

		while ((z = reader.read()) != -1) {
			separator = (char) z;
			maxvecs = vec.size();

			for (count = 0; count < maxvecs; count++) {
				v = vec.get(count);
				n = ((Integer) v[2]).intValue();
				c = getSuccessor(((Character) v[0]).charValue(), n);

				if (!Character.isLetterOrDigit(separator)) {
					if ((c == ((Character) v[0]).charValue()) && (c != 'S')) {
						vecRemovelist.add(v);
					} else {
						((StringBuilder) v[1]).append(separator);

						if ((c == 'X') || (c == 'Y')) {
							v[2] = Integer.valueOf(4);
						}
					}
				} else {
					if (c == 'X') {
						c = 'y';
						nv = new Object[3];
						nv[1] = (new StringBuilder(((StringBuilder) v[1]).toString())).append('M');
						nv[0] = Character.valueOf('M');
						nv[2] = Integer.valueOf(1);
						vec.add(nv);
					} else if (c == 'Y') {
						c = 'M';
						nv = new Object[3];
						nv[1] = (new StringBuilder(((StringBuilder) v[1]).toString())).append('d');
						nv[0] = Character.valueOf('d');
						nv[2] = Integer.valueOf(1);
						vec.add(nv);
					}

					((StringBuilder) v[1]).append(c);

					if (c == ((Character) v[0]).charValue()) {
						v[2] = Integer.valueOf(n + 1);
					} else {
						v[0] = Character.valueOf(c);
						v[2] = Integer.valueOf(1);
					}
				}
			}

			int size = vecRemovelist.size();

			for (int i = 0; i < size; i++) {
				v = vecRemovelist.get(i);
				vec.remove(v);
			}

			vecRemovelist.clear();
		}

		int size = vec.size();

		for (int i = 0; i < size; i++) {
			v = vec.get(i);
			c = ((Character) v[0]).charValue();
			n = ((Integer) v[2]).intValue();

			boolean bk = getSuccessor(c, n) != c;
			boolean atEnd = (((c == 's') || (c == 'm') || ((c == 'h') && toTime)) && bk);
			boolean finishesAtDate = (bk && (c == 'd') && !toTime);
			boolean containsEnd = (((StringBuilder) v[1]).toString().indexOf('W') != -1);

			if ((!atEnd && !finishesAtDate) || (containsEnd)) {
				vecRemovelist.add(v);
			}
		}

		size = vecRemovelist.size();

		for (int i = 0; i < size; i++) {
			vec.remove(vecRemovelist.get(i));
		}

		vecRemovelist.clear();
		v = vec.get(0); // might throw exception

		StringBuilder format = (StringBuilder) v[1];
		format.setLength(format.length() - 1);

		return format.toString();
	}

	default char getSuccessor(char c, int n) {
		return ((c == 'y') && (n == 2)) ? 'X'
				: (((c == 'y') && (n < 4)) ? 'y' : ((c == 'y') ? 'M' : (((c == 'M') && (n == 2)) ? 'Y'
				: (((c == 'M') && (n < 3)) ? 'M' : ((c == 'M') ? 'd' : (((c == 'd') && (n < 2)) ? 'd' : ((c == 'd') ? 'H' : (((c == 'H') && (n < 2))
				? 'H' : ((c == 'H') ? 'm'
				: (((c == 'm') && (n < 2)) ? 'm' : ((c == 'm') ? 's' : (((c == 's') && (n < 2)) ? 's' : 'W'))))))))))));
	}


}
