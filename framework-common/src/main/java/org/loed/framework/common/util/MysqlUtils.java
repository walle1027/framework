package org.loed.framework.common.util;


import org.loed.framework.common.orm.Column;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/11/7 10:00 上午
 */
public class MysqlUtils {
	public static String getEscapedSqlVal(Object val, Column column){
		return  "'" + StringHelper.escapeSql(getSqlForNotNullValue(val, column)) + "'";
	}
	public static String getSqlForNotNullValue(Object val, Column column) {
		try {
			SQLType sqlType = column.getSqlType();
			switch (sqlType.getVendorTypeNumber()) {
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
					return getNumericObject(val, sqlType.getVendorTypeNumber(), scale);
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
						switch (sqlType.getVendorTypeNumber()) {
							case Types.DATE:
								SimpleDateFormat ddf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
								return ddf.format(new java.sql.Date(parameterAsDate.getTime()));
							case Types.TIMESTAMP:
								SimpleDateFormat tsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
								return tsdf.format(new Timestamp(parameterAsDate.getTime())) + ".0";
							default:
								break;
						}
					} else if (val instanceof java.util.Date) {
						parameterAsDate = (java.util.Date) val;
						switch (sqlType.getVendorTypeNumber()) {
							case Types.DATE:
								SimpleDateFormat ddf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
								if (parameterAsDate instanceof java.sql.Date) {
									return ddf.format(parameterAsDate);
								} else {
									return ddf.format(new java.sql.Date(parameterAsDate.getTime()));
								}
							case Types.TIMESTAMP:
								SimpleDateFormat tsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
								if (parameterAsDate instanceof Timestamp) {
									return tsdf.format(parameterAsDate) + ".0";
								} else {
									return tsdf.format(new Timestamp(parameterAsDate.getTime())) + ".0";
								}
							default:
								break;
						}
					} else if (val instanceof LocalDateTime) {
						return ((LocalDateTime) val).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
					} else if (val instanceof java.time.LocalDate) {
						return ((java.time.LocalDate) val).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					} else {
						throw new RuntimeException("unsupported date type:" + val.getClass() + " for column:" + column.getJavaName());
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


	public static String getNumericObject(Object val, int sqlType, int scale) {
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
					BigDecimal scaledBigDecimal;

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
					return StringHelper.fixDecimalExponent(StringHelper.consistentToString(BigDecimal.valueOf(parameterAsNum.doubleValue())));
				}
		}
		throw new RuntimeException("convert to number fail " + val.getClass().getName() + "   " + val.toString());
	}

	public static String getBoolean(boolean b) {
		return b ? "1" : "0";
	}

	public static String getDateTimePattern(String dt, boolean toTime) throws Exception {
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
		ArrayList<Object[]> vec = new ArrayList<>();
		ArrayList<Object[]> vecRemovelist = new ArrayList<>();
		Object[] nv = new Object[3];
		Object[] v;
		nv[0] = 'y';
		nv[1] = new StringBuilder();
		nv[2] = 0;
		vec.add(nv);

		if (toTime) {
			nv = new Object[3];
			nv[0] = 'h';
			nv[1] = new StringBuilder();
			nv[2] = 0;
			vec.add(nv);
		}

		while ((z = reader.read()) != -1) {
			separator = (char) z;
			maxvecs = vec.size();

			for (count = 0; count < maxvecs; count++) {
				v = vec.get(count);
				n = (Integer) v[2];
				c = getSuccessor((Character) v[0], n);

				if (!Character.isLetterOrDigit(separator)) {
					if ((c == (Character) v[0]) && (c != 'S')) {
						vecRemovelist.add(v);
					} else {
						((StringBuilder) v[1]).append(separator);

						if ((c == 'X') || (c == 'Y')) {
							v[2] = 4;
						}
					}
				} else {
					if (c == 'X') {
						c = 'y';
						nv = new Object[3];
						nv[1] = (new StringBuilder(((StringBuilder) v[1]).toString())).append('M');
						nv[0] = 'M';
						nv[2] = 1;
						vec.add(nv);
					} else if (c == 'Y') {
						c = 'M';
						nv = new Object[3];
						nv[1] = (new StringBuilder(((StringBuilder) v[1]).toString())).append('d');
						nv[0] = 'd';
						nv[2] = 1;
						vec.add(nv);
					}

					((StringBuilder) v[1]).append(c);

					if (c == (Character) v[0]) {
						v[2] = n + 1;
					} else {
						v[0] = c;
						v[2] = 1;
					}
				}
			}

			int size = vecRemovelist.size();

			for (Object[] objects : vecRemovelist) {
				v = objects;
				vec.remove(v);
			}

			vecRemovelist.clear();
		}

		int size = vec.size();

		for (int i = 0; i < size; i++) {
			v = vec.get(i);
			c = (Character) v[0];
			n = (Integer) v[2];

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

	private static char getSuccessor(char c, int n) {
		return ((c == 'y') && (n == 2)) ? 'X'
				: (((c == 'y') && (n < 4)) ? 'y' : ((c == 'y') ? 'M' : (((c == 'M') && (n == 2)) ? 'Y'
				: (((c == 'M') && (n < 3)) ? 'M' : ((c == 'M') ? 'd' : (((c == 'd') && (n < 2)) ? 'd' : ((c == 'd') ? 'H' : (((c == 'H') && (n < 2))
				? 'H' : ((c == 'H') ? 'm'
				: (((c == 'm') && (n < 2)) ? 'm' : ((c == 'm') ? 's' : (((c == 's') && (n < 2)) ? 's' : 'W'))))))))))));
	}
}
