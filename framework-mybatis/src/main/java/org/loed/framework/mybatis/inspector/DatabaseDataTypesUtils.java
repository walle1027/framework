//package org.loed.framework.mybatis.inspector;
//
//import java.sql.Types;
//import java.util.HashMap;
//
///**
// * @author yangtao
// * @email walle1027@gmail.com
// */
//public class DatabaseDataTypesUtils {
//
//	private final static IntStringMap _preferredJavaTypeForSqlType = new IntStringMap();
//
//	static {
//		_preferredJavaTypeForSqlType.put(Types.TINYINT, "java.lang.Byte");
//		_preferredJavaTypeForSqlType.put(Types.SMALLINT, "java.lang.Short");
//		_preferredJavaTypeForSqlType.put(Types.INTEGER, "java.lang.Integer");
//		_preferredJavaTypeForSqlType.put(Types.BIGINT, "java.lang.Long");
//		_preferredJavaTypeForSqlType.put(Types.REAL, "java.lang.Float");
//		_preferredJavaTypeForSqlType.put(Types.FLOAT, "java.lang.Double");
//		_preferredJavaTypeForSqlType.put(Types.DOUBLE, "java.lang.Double");
//		_preferredJavaTypeForSqlType.put(Types.DECIMAL, "java.math.BigDecimal");
//		_preferredJavaTypeForSqlType.put(Types.NUMERIC, "java.math.BigDecimal");
//		_preferredJavaTypeForSqlType.put(Types.BIT, "java.lang.Boolean");
//		_preferredJavaTypeForSqlType.put(Types.CHAR, "java.lang.String");
//		_preferredJavaTypeForSqlType.put(Types.VARCHAR, "java.lang.String");
//		// according to resultset.gif, we should use java.io.Reader, but String
//		// is more convenient for EJB
//		_preferredJavaTypeForSqlType.put(Types.LONGVARCHAR, "java.lang.String");
//		_preferredJavaTypeForSqlType.put(Types.BINARY, "byte[]");
//		_preferredJavaTypeForSqlType.put(Types.VARBINARY, "byte[]");
//		_preferredJavaTypeForSqlType.put(Types.LONGVARBINARY,
//				"java.io.InputStream");
//
//		// 根据项目需要，自己修改了类型对应关系
//		// _preferredJavaTypeForSqlType.put(Types.DATE, "java.sql.Date");
//		// _preferredJavaTypeForSqlType.put(Types.TIME, "java.sql.Time");
//		// _preferredJavaTypeForSqlType.put(Types.TIMESTAMP,
//		// "java.sql.Timestamp");
//
//		// 把所有的时间类型都改成java.util.date类型的数据
//		_preferredJavaTypeForSqlType.put(Types.DATE, "java.util.Date");
//		_preferredJavaTypeForSqlType.put(Types.TIME, "java.util.Date");
//		_preferredJavaTypeForSqlType.put(Types.TIMESTAMP, "java.util.Date");
//
//		// 把clob，类型的数据全部改成String型
//		// _preferredJavaTypeForSqlType.put(Types.CLOB, "java.sql.Clob");
//		// _preferredJavaTypeForSqlType.put(Types.BLOB, "java.sql.Blob");
//		_preferredJavaTypeForSqlType.put(Types.CLOB, "java.lang.String");
//		_preferredJavaTypeForSqlType.put(Types.BLOB, "java.sql.Blob");
//		_preferredJavaTypeForSqlType.put(Types.NCLOB, "java.lang.String");
//
//		_preferredJavaTypeForSqlType.put(Types.ARRAY, "java.sql.Array");
//		_preferredJavaTypeForSqlType.put(Types.REF, "java.sql.Ref");
//		_preferredJavaTypeForSqlType.put(Types.STRUCT, "java.lang.Object");
//		_preferredJavaTypeForSqlType.put(Types.JAVA_OBJECT, "java.lang.Object");
//	}
//
//	public static boolean isFloatNumber(int sqlType, int size, int decimalDigits) {
//		String javaType = getPreferredJavaType(sqlType, size, decimalDigits);
//		if (javaType.endsWith("Float") || javaType.endsWith("Double")
//				|| javaType.endsWith("BigDecimal")) {
//			return true;
//		}
//		return false;
//	}
//
//	public static boolean isIntegerNumber(int sqlType, int size,
//	                                      int decimalDigits) {
//		String javaType = getPreferredJavaType(sqlType, size, decimalDigits);
//		if (javaType.endsWith("Long") || javaType.endsWith("Integer")
//				|| javaType.endsWith("Short")) {
//			return true;
//		}
//		return false;
//	}
//
//	public static boolean isDate(int sqlType, int size, int decimalDigits) {
//		String javaType = getPreferredJavaType(sqlType, size, decimalDigits);
//		if (javaType.endsWith("Date") || javaType.endsWith("Timestamp")) {
//			return true;
//		}
//		return false;
//	}
//
//	public static String getPreferredJavaType(int sqlType, int size,
//	                                          int decimalDigits) {
//		if ((sqlType == Types.DECIMAL || sqlType == Types.NUMERIC)
//				&& decimalDigits == 0) {
//			// 根据项目需求自己修改了数据类型，不再判断布尔，字节，短字节等类型
//			// 如果有需要，去掉下面的注释
//			// if (size == 1) {
//			// https://sourceforge.net/tracker/?func=detail&atid=415993&aid=
//			// 662953&group_id=36044
//			// return "java.lang.Boolean";
//			// } else if (size < 3) {
//			// return "java.lang.Byte";
//			// } else if (size < 5) {
//			// return "java.lang.Short";
//			// } else
//			if (size < 10) {
//				return "java.lang.Integer";
//			} else if (size < 19) {
//				return "java.lang.Long";
//			} else {
//				return "java.math.BigDecimal";
//			}
//		}
//		String result = _preferredJavaTypeForSqlType.getString(sqlType);
//		if (result == null) {
//			// oracle 不识别nclob等数据类型 暂时不能识别的类型先处理成String
//			// result = "java.lang.Object";
//			result = "java.lang.String";
//		}
//		return result;
//	}
//
//	@SuppressWarnings({"serial", "unchecked"})
//	private static class IntStringMap extends HashMap {
//
//		public String getString(int i) {
//			return (String) get(new Integer(i));
//		}
//
//		public String[] getStrings(int i) {
//			return (String[]) get(new Integer(i));
//		}
//
//		@SuppressWarnings("unchecked")
//		public void put(int i, String s) {
//			put(new Integer(i), s);
//		}
//
//		public void put(int i, String[] sa) {
//			put(new Integer(i), sa);
//		}
//	}
//
//}
