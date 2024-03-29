package org.loed.framework.common.data;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.loed.framework.common.util.LocalDateUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

@SuppressWarnings({"RedundantCast"})
@Slf4j
public class DataType {
	/**
	 * basic types
	 */
	public static final int DT_Unknown = 0;
	public static final int DT_byte = 1;
	public static final int DT_short = 2;
	public static final int DT_int = 3;
	public static final int DT_long = 4;
	public static final int DT_float = 5;
	public static final int DT_double = 6;
	public static final int DT_char = 7;
	public static final int DT_boolean = 8;
	public static final int DT_Byte = 10;
	public static final int DT_Short = 11;
	public static final int DT_Integer = 12;
	public static final int DT_Long = 13;
	public static final int DT_Float = 14;
	public static final int DT_Double = 15;
	public static final int DT_Character = 16;
	public static final int DT_Boolean = 17;
	public static final int DT_String = 20;
	public static final int DT_BigInteger = 21;
	public static final int DT_BigDecimal = 22;
	public static final int DT_Date = 23;
	public static final int DT_Time = 24;
	public static final int DT_DateTime = 25;
	public static final int DT_LocalDate = 61;
	public static final int DT_LocalDateTime = 62;
	public static final int DT_ZoneDateTime = 63;

	/**
	 * sql types
	 */
	public static final int DT_Clob = 26;
	public static final int DT_Blob = 27;

	/**
	 * collection types
	 */
	public static final int DT_Array = 30;
	public static final int DT_List = 31;
	public static final int DT_Map = 34;
	public static final int DT_Set = 37;

	/**
	 * object types
	 */
	public static final int DT_Object = 40;
	public static final int DT_Class = 41;
	public static final int DT_Enum = 42;
	public static final int DT_UserDefine = 50;
	private static Map<String, Integer> dataTypeMap = new Hashtable<String, Integer>();

	public static final DateTimeFormatter yyyyMMddHHmmssSSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	public static final DateTimeFormatter yyyyMMddHHmmssSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");
	public static final DateTimeFormatter yyyyMMddHHmmssS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
	public static final DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter yyyyMMddHHmm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	public static final DateTimeFormatter yyyyMMddHH = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
	public static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	static {
		dataTypeMap.put("byte", DT_byte);
		dataTypeMap.put("Byte", DT_Byte);
		dataTypeMap.put("short", DT_short);
		dataTypeMap.put("Short", DT_Short);
		dataTypeMap.put("int", DT_int);
		dataTypeMap.put("Integer", DT_Integer);
		dataTypeMap.put("long", DT_long);
		dataTypeMap.put("Long", DT_Long);
		dataTypeMap.put("boolean", DT_boolean);
		dataTypeMap.put("Boolean", DT_Boolean);
		dataTypeMap.put("char", DT_char);
		dataTypeMap.put("Character", DT_Character);
		dataTypeMap.put("float", DT_float);
		dataTypeMap.put("Float", DT_Float);
		dataTypeMap.put("double", DT_double);
		dataTypeMap.put("Double", DT_Double);
		dataTypeMap.put("BigInteger", DT_BigInteger);
		dataTypeMap.put("BigDecimal", DT_BigDecimal);
		dataTypeMap.put("String", DT_String);
		dataTypeMap.put("Date", DT_Date);
		dataTypeMap.put("Time", DT_Time);
		dataTypeMap.put("Timestamp", DT_DateTime);
		dataTypeMap.put("LocalDateTime", DT_LocalDateTime);
		dataTypeMap.put("LocalDate", DT_LocalDate);
		dataTypeMap.put("ZoneDateTime", DT_ZoneDateTime);
		dataTypeMap.put("List", DT_List);
		dataTypeMap.put("ArrayList", DT_List);
		dataTypeMap.put("LinkedList", DT_List);
		dataTypeMap.put("Map", DT_Map);
		dataTypeMap.put("HashMap", DT_Map);
		dataTypeMap.put("Hashtable", DT_Map);
		dataTypeMap.put("Set", DT_Set);
		dataTypeMap.put("HashSet", DT_Set);
		dataTypeMap.put("Object", DT_Object);
		dataTypeMap.put("Class", DT_Class);
		dataTypeMap.put("clob", DT_Clob);
		dataTypeMap.put("blob", DT_Blob);
	}

	private static String deletePrefix(String str, String prefix) {
		if (str.startsWith(prefix)) {
			str = str.substring(prefix.length());
		}
		return str;
	}

	public static String toSimpleType(String typeName) {
		typeName = deletePrefix(typeName, "class ");
		typeName = deletePrefix(typeName, "java.lang.");
		typeName = deletePrefix(typeName, "java.util.");
		typeName = deletePrefix(typeName, "java.sql.");
		typeName = deletePrefix(typeName, "java.math.");
		//增加对localDateTime的支持
		typeName = deletePrefix(typeName, "java.time.");
		return typeName;
	}

	public static int getDataType(Object obj) {
		if (obj == null) {
			return DT_Unknown;
		}
		if (obj.getClass().isEnum()) {
			return DT_Enum;
		}
		if (obj.getClass().isArray()) {
			return DT_Array;
		}
		return getDataType(obj.getClass());
	}

	@SuppressWarnings("unchecked")
	public static int getDataType(Class<?> cls) {
		if (cls == null) {
			return DT_Unknown;
		}
		if (cls.isArray()) {
			return DT_Array;
		}
		if (cls.isEnum()) {
			return DT_Enum;
		}
		String typeName = toSimpleType(cls.getName());

		if (typeName.charAt(0) == '[') {
			return DT_Array;
		}
		Integer iType = dataTypeMap.get(typeName);
		return iType == null ? DT_UserDefine : iType;
	}

	public static String toUnifyTypeName(String sName) {
		return matchBracket(sName, "<", ">", false);
	}

	public static String getElementTypeName(String collectionTypeName) {
		return getElementTypeName(collectionTypeName, 0);
	}

	public static String getElementTypeName(String collectionTypeName,
	                                        int itemIndex) {
		String typeName = toSimpleType(collectionTypeName);
		int iType = getDataType(typeName);
		if (iType == DT_Array) {
			switch (typeName.charAt(1)) {
				case 'B': // byte[]
					return "byte";
				case 'S': // short[]
					return "short";
				case 'I': // int[]
					return "int";
				case 'J': // long[]
					return "long";
				case 'Z': // boolean[]
					return "boolean";
				case 'C': // char[]
					return "char";
				case 'F': // float[]
					return "float";
				case 'D': // double[]
					return "double";
				case 'L': // [Ljava.lang.Integer;
					if (typeName.charAt(typeName.length() - 1) == ';') {
						return typeName.substring(2, typeName.length() - 1);
					} else {
						return typeName.substring(2);
					}
				case '[': // [[I
					return typeName.substring(1);
			}
		}

		String str = matchBracket(typeName, "<", ">", true);
		int iLen = str.length();
		for (int i = 0; i <= itemIndex; i++) {
			str = matchBracket(str, "<", ">", false);
			int iLen1 = str.length();
			if (iLen1 == iLen) { // 没有可以切除的部分了
				break;
			} else {
				iLen = iLen1;
			}
		}

		// 需找逗号分隔符
		int iBegin = 0, iEnd = str.length();
		int iPos = 0;
		for (int i = 0; i < itemIndex + 1; i++) {
			iPos = str.indexOf(',', iPos);
			if (iPos == -1) {
				break;
			}

			if (i == itemIndex - 1) {
				iBegin = iPos;
			} else if (i == itemIndex) {
				iEnd = iPos;
			}
		}

		return str.substring(iBegin + 1, iEnd);
	}

	public static int getElementDataType(String collectionTypeName) {
		return getElementDataType(collectionTypeName, 0);
	}

	public static int getElementDataType(String collectionTypeName,
	                                     int itemIndex) {
		return getDataType(getElementTypeName(collectionTypeName, itemIndex));
	}
	// public static final int DT_HashSet = 38;

	public static Object toType(Object value, String targetType) {
		int destType = getDataType(targetType);
		return toType(value, destType);
	}

	public static Object toType(Object value, int targetType) {
		int srcType = getDataType(value);
		return toType(value, srcType, targetType);
	}

	public static Object toType(Object value, int srcType, int targetType) {
		srcType = toObjectType(srcType);
		targetType = toObjectType(targetType);
		if (srcType == targetType) {
			return value;
		}

		if (value == null) {
			return null;
		}

		Object retObj = null;
		if (srcType == DT_String) {
			String str = ((String) value).trim();
			if (str.length() < 1 || str.equalsIgnoreCase("null")) {
				return null;
			}
		}

		if (srcType >= DT_byte && srcType <= DT_boolean) {
			srcType += DT_Byte - DT_byte;
		}
		if (targetType >= DT_byte && targetType <= DT_boolean) {
			targetType += DT_Byte - DT_byte;
		}
		//大小类型转换
		if (srcType == targetType) {
			return value;
		}

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			switch (targetType) {
				case DT_Byte:
					retObj = convertToByte(srcType, value);
					break;
				case DT_Short:
					retObj = convertToShort(srcType, value);
					break;
				case DT_Integer:
					//TODO extract method
					switch (srcType) {
						case DT_Byte:
							retObj = ((Byte) value).intValue();
							break;
						case DT_Short:
							retObj = ((Short) value).intValue();
							break;
						case DT_Long:
							retObj = ((Long) value).intValue();
							break;
						case DT_BigInteger:
							retObj = ((BigInteger) value).intValue();
							break;
						case DT_Float:
							retObj = ((Float) value).intValue();
							break;
						case DT_Double:
							retObj = ((Double) value).intValue();
							break;
						case DT_BigDecimal:
							retObj = ((BigDecimal) value).intValue();
							break;
						case DT_Character:
							retObj = Integer.parseInt(((Character) value).toString());
							break;
						case DT_String:
							String strValue = value.toString().replace(",", "");
							retObj = Integer.parseInt(strValue);
							break;
						case DT_Boolean:
							retObj = (int) ((Boolean) value ? 1 : 0);
							break;
						case DT_Date:
							retObj = (int) ((java.util.Date) value).getTime();
							break;
						case DT_Time:
							retObj = (int) ((java.sql.Time) value).getTime();
							break;
						case DT_DateTime:
							retObj = (int) ((java.sql.Timestamp) value).getTime();
							break;
					}
					break;
				case DT_Long:
					switch (srcType) {
						case DT_Byte:
							retObj = ((Byte) value).longValue();
							break;
						case DT_Short:
							retObj = ((Short) value).longValue();
							break;
						case DT_Integer:
							retObj = ((Integer) value).longValue();
							break;
						case DT_BigInteger:
							retObj = ((BigInteger) value).longValue();
							break;
						case DT_Float:
							retObj = ((Float) value).longValue();
							break;
						case DT_Double:
							retObj = ((Double) value).longValue();
							break;
						case DT_BigDecimal:
							retObj = ((BigDecimal) value).longValue();
							break;
						case DT_Character:
							retObj = Long.parseLong(((Character) value).toString());
							break;
						case DT_String:
							String strValue = value.toString().replace(",", "");
							retObj = Long.parseLong(strValue);
							break;
						case DT_Boolean:
							retObj = (long) ((Boolean) value ? 1 : 0);
							break;
						case DT_Date:
							retObj = ((java.util.Date) value).getTime();
							break;
						case DT_Time:
							retObj = ((java.sql.Time) value).getTime();
							break;
						case DT_DateTime:
							retObj = ((java.sql.Timestamp) value).getTime();
							break;
					}
					break;
				case DT_BigInteger:
					switch (srcType) {
						case DT_Byte:
							retObj = BigInteger.valueOf(((Byte) value).longValue());
							break;
						case DT_Short:
							retObj = BigInteger.valueOf(((Short) value).longValue());
							break;
						case DT_Integer:
							retObj = BigInteger.valueOf(((Integer) value).longValue());
							break;
						case DT_Long:
							retObj = BigInteger.valueOf((Long) value);
							break;
						case DT_Float:
							retObj = BigInteger.valueOf(((Float) value).longValue());
							break;
						case DT_Double:
							retObj = BigInteger.valueOf(((Double) value).longValue());
							break;
						case DT_BigDecimal:
							retObj = BigInteger.valueOf(((BigDecimal) value).longValue());
							break;
						case DT_Character:
							retObj = BigInteger.valueOf(Long.parseLong(((Character) value).toString()));
							break;
						case DT_String:
							String strValue = value.toString().replace(",", "");
							retObj = BigInteger.valueOf(Long.parseLong(strValue));
							break;
						case DT_Boolean:
							retObj = BigInteger.valueOf(((Boolean) value ? 1 : 0));
							break;
						case DT_Date:
							retObj = BigInteger.valueOf(((java.util.Date) value).getTime());
							break;
						case DT_Time:
							retObj = BigInteger.valueOf(((java.sql.Time) value).getTime());
							break;
						case DT_DateTime:
							retObj = BigInteger.valueOf(((java.sql.Timestamp) value).getTime());
							break;
					}
					break;
				case DT_Float:
					switch (srcType) {
						case DT_Byte:
							retObj = ((Byte) value).floatValue();
							break;
						case DT_Short:
							retObj = ((Short) value).floatValue();
							break;
						case DT_Integer:
							retObj = ((Integer) value).floatValue();
							break;
						case DT_Long:
							retObj = ((Long) value).floatValue();
							break;
						case DT_BigInteger:
							retObj = ((BigInteger) value).floatValue();
							break;
						case DT_Double:
							retObj = ((Double) value).floatValue();
							break;
						case DT_BigDecimal:
							retObj = ((BigDecimal) value).floatValue();
							break;
						case DT_Character:
							retObj = Float.parseFloat(((Character) value).toString());
							break;
						case DT_String:
							String strValue = value.toString().replace(",", "");
							retObj = Float.parseFloat(strValue);
							break;
						case DT_Boolean:
							retObj = (float) ((Boolean) value ? 1 : 0);
							break;
					}
					break;
				case DT_BigDecimal:
					switch (srcType) {
						case DT_Byte:
							retObj = BigDecimal.valueOf(((Byte) value).doubleValue());
							break;
						case DT_Short:
							retObj = BigDecimal.valueOf(((Short) value).doubleValue());
							break;
						case DT_Integer:
							retObj = BigDecimal.valueOf((Integer) value);
							break;
						case DT_Long:
							retObj = BigDecimal.valueOf((Long) value);
							break;
						case DT_Double:
							retObj = BigDecimal.valueOf((Double) value);
							break;
						case DT_Float:
							retObj = BigDecimal.valueOf(((Float) value).doubleValue());
							break;
						case DT_Character:
							retObj = BigDecimal.valueOf(Double.parseDouble(((Character) value).toString()));
							break;
						case DT_String:
							String strValue = value.toString().replace(",", "");
							retObj = BigDecimal.valueOf(Double.parseDouble(strValue));
							break;
						case DT_Boolean:
							retObj = ((Boolean) value ? BigDecimal.ONE : BigDecimal.ZERO);
							break;
					}
					break;
				case DT_Double:
					switch (srcType) {
						case DT_Byte:
							retObj = ((Byte) value).doubleValue();
							break;
						case DT_Short:
							retObj = ((Short) value).doubleValue();
							break;
						case DT_Integer:
							retObj = ((Integer) value).doubleValue();
							break;
						case DT_Long:
							retObj = ((Long) value).doubleValue();
							break;
						case DT_BigInteger:
							retObj = ((BigInteger) value).doubleValue();
							break;
						case DT_Float:
							retObj = ((Float) value).doubleValue();
							break;
						case DT_BigDecimal:
							retObj = ((BigDecimal) value).doubleValue();
							break;
						case DT_Character:
							retObj = Double.parseDouble(((Character) value).toString());
							break;
						case DT_String:
							String strValue = value.toString().replace(",", "");
							retObj = Double.parseDouble(strValue);
							break;
						case DT_Boolean:
							retObj = (double) ((Boolean) value ? 1 : 0);
							break;
					}
					break;
				case DT_Character:
					switch (srcType) {
						case DT_Byte:
							retObj = Character.toChars((Byte) value)[0];
							break;
						case DT_Short:
							retObj = Character.toChars((Short) value)[0];
							break;
						case DT_Integer:
							retObj = Character.toChars((Integer) value)[0];
							break;
						case DT_Long:
							retObj = Character.toChars(((Long) value).intValue())[0];
							break;
						case DT_BigInteger:
							retObj = Character.toChars(((BigInteger) value).intValue())[0];
							break;
						case DT_Float:
							retObj = Character.toChars(((Float) value).intValue())[0];
							break;
						case DT_Double:
							retObj = Character.toChars(((Double) value).intValue())[0];
							break;
						case DT_BigDecimal:
							retObj = Character.toChars(((BigDecimal) value).intValue())[0];
							break;
						case DT_String:
							retObj = ((String) value).charAt(0);
							break;
						case DT_Boolean:
							retObj = (double) ((Boolean) value ? 'T' : 'F');
							break;
					}
					break;
				case DT_Boolean:
					switch (srcType) {
						case DT_Byte:
							retObj = (Byte) value != 0;
							break;
						case DT_Short:
							retObj = (Short) value != 0;
							break;
						case DT_Integer:
							retObj = (Integer) value != 0;
							break;
						case DT_Long:
							retObj = (Long) value != 0;
							break;
						case DT_BigInteger:
							retObj = ((BigInteger) value).longValue() != 0;
							break;
						case DT_Float:
							retObj = ((Float) value).intValue() != 0;
							break;
						case DT_Double:
							retObj = ((Double) value).intValue() != 0;
							break;
						case DT_BigDecimal:
							retObj = ((BigDecimal) value).longValue() != 0;
							break;
						case DT_Character:
							retObj = (Character) value == 'T';
							break;
						case DT_String:
							String strValue = (String) value;
							if (strValue.equalsIgnoreCase("true")) {
								retObj = true;
							} else if (strValue.equalsIgnoreCase("t")) {
								retObj = true;
							} else if (strValue.equalsIgnoreCase("yes")) {
								retObj = true;
							} else if (strValue.equalsIgnoreCase("y")) {
								retObj = true;
							} else {
								retObj = strValue.equalsIgnoreCase("是");
							}
							break;
					}
					break;
				case DT_String:
					switch (srcType) {
						case DT_Byte:
							retObj = ((Byte) value).toString();
							break;
						case DT_Short:
							retObj = ((Short) value).toString();
							break;
						case DT_Integer:
							retObj = ((Integer) value).toString();
							break;
						case DT_Long:
							retObj = ((Long) value).toString();
							break;
						case DT_BigInteger:
							retObj = ((BigInteger) value).toString();
							break;
						case DT_Float:
							retObj = ((Float) value).toString();
							break;
						case DT_Double:
							retObj = ((Double) value).toString();
							break;
						case DT_BigDecimal:
							retObj = ((BigDecimal) value).toString();
							break;
						case DT_Character:
							retObj = ((Character) value).toString();
							break;
						case DT_Boolean:
							retObj = ((Boolean) value).toString();
							break;
						case DT_Date:
							if (value instanceof java.sql.Date) {
								sdf.applyPattern("yyyy-MM-dd");
							}
							retObj = sdf.format((java.util.Date) value);
							break;
						case DT_Time:
							sdf.applyPattern("HH:mm:ss");
							retObj = sdf.format((java.util.Date) value);
							break;
						case DT_DateTime:
							retObj = sdf.format((java.util.Date) value);
							break;
					}
					break;
				case DT_Date:
					switch (srcType) {
						case DT_String:
							retObj = DateUtils.parseDate(value.toString(),
									"yyyy-MM-dd HH:mm:ss.SSS"
									, "yyyy-MM-dd HH:mm:ss"
									, "yyyy-MM-dd HH:mm"
									, "yyyy-MM-dd HH"
									, "yyyy-MM-dd"
									, "yyyy-MM"
									, "yyyy");
							break;
						case DT_DateTime:
						case DT_Long:
						case DT_Integer:
							Calendar cal = Calendar.getInstance();
							if (srcType == DT_DateTime) {
								cal.setTime((java.util.Date) value);
							} else if (srcType == DT_Long) {
								cal.setTimeInMillis((Long) value);
							} else {
								cal.setTimeInMillis((Integer) value);
							}
							cal.set(Calendar.HOUR_OF_DAY, 0);
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND, 0);
							cal.set(Calendar.MILLISECOND, 0);
							retObj = new java.sql.Date(cal.getTimeInMillis());
							break;
					}
					break;
				case DT_Time:
					switch (srcType) {
						case DT_String:
							// sdf.applyPattern("HH:mm:ss");
							// date = sdf.parse((String)value);
							// retObj = new java.sql.Time(date.getTime());
							retObj = java.sql.Time.valueOf((String) value);
							break;
						case DT_DateTime:
						case DT_Long:
						case DT_Integer:
							Calendar cal = Calendar.getInstance();
							if (srcType == DT_DateTime) {
								cal.setTime((java.util.Date) value);
							} else if (srcType == DT_Long) {
								cal.setTimeInMillis((Long) value);
							} else {
								cal.setTimeInMillis((Integer) value);
							}
							cal.set(Calendar.YEAR, 0);
							cal.set(Calendar.MONTH, 0);
							cal.set(Calendar.DAY_OF_MONTH, 0);
							cal.set(Calendar.MILLISECOND, 0);
							retObj = new java.sql.Time(cal.getTimeInMillis());
							break;
					}
					break;
				case DT_DateTime:
					switch (srcType) {
						case DT_String:
							// date = sdf.parse((String)value);
							// retObj = new java.sql.Timestamp(date.getTime());
							retObj = java.sql.Timestamp.valueOf((String) value);
							break;
						case DT_Date:
						case DT_Time:
						case DT_Long:
						case DT_Integer:
							retObj = new java.sql.Timestamp(Long.valueOf(String.valueOf(value)));
							break;
					}
					break;
				case DT_LocalDate:
					retObj = convert2LocalDate(srcType, value);
					break;
				case DT_LocalDateTime:
					retObj = convert2LocalDateTime(srcType, value);
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retObj;
	}

	private static String matchBracket(String str, String matchBegin,
	                                   String matchEnd, boolean bGet) {
		int iLen = str.length();
		int iMatchLen = matchBegin.length();
		char cBegin = '[', cEnd = ']';
		int iBeginPos = -1, iEndPos = -1;
		boolean bFindMatch = false;
		int iCount = -1;
		for (int i = 0; i < iLen; i++) {
			char ch = str.charAt(i);
			if (bFindMatch) {
				if (ch == cBegin) {
					iCount++;
				} else if (ch == cEnd) {
					iCount--;
					if (iCount == 0) {
						iEndPos = i;
						break;
					}
				}
			} else {
				for (int k = 0; k < iMatchLen; k++) {
					cBegin = matchBegin.charAt(k);
					if (ch == cBegin) {
						bFindMatch = true;
						cEnd = matchEnd.charAt(k);
						iBeginPos = i;
						iCount = 1;
						break;
					}
				}
			}
		}

		if (bFindMatch) { // 找到匹配
			if (bGet) { // 截取匹配括号中的子串
				return str.substring(iBeginPos + 1, iEndPos);
			} else { // 切除匹配括号中的字串
				return str.substring(0, iBeginPos) + str.substring(iEndPos + 1);
			}
		} else { // 未找到匹配
			if (bGet) { // 截取匹配括号中的子串
				return "";
			} else { // 切除匹配括号中的字串
				return str;
			}
		}
	}

	public static boolean isSimpleType(int iType) {
		switch (iType) {
			case DT_byte:
			case DT_short:
			case DT_int:
			case DT_long:
			case DT_float:
			case DT_double:
			case DT_char:
			case DT_boolean:

			case DT_Byte:
			case DT_Short:
			case DT_Integer:
			case DT_Long:
			case DT_BigInteger:
			case DT_Float:
			case DT_Double:
			case DT_BigDecimal:
			case DT_Character:
			case DT_String:
			case DT_Boolean:
			case DT_Date:
			case DT_Time:
			case DT_DateTime:
			case DT_LocalDate:
			case DT_LocalDateTime:
			case DT_ZoneDateTime:
			case DT_Clob:
			case DT_Blob:
				return true;
		}

		return false;
	}

	public static int toObjectType(int iType) {
		if (iType >= DT_byte && iType <= DT_boolean) {
			iType += DT_Byte - DT_byte;
		}
		return iType;
	}

	/**
	 * 判断一个类型是不是集合类型
	 *
	 * @param dataType 数据类型
	 * @return true 集合类型，false 非集合类型
	 */
	public static boolean isCollectionType(int dataType) {
		switch (dataType) {
			case DT_Array:
			case DT_List:
			case DT_Set:
				return true;
		}
		return false;
	}

	/**
	 * 判断属性是不是Map类型
	 *
	 * @param dataType 数据分类
	 * @return true：map类型，false非map类型
	 */
	public static boolean isMapType(int dataType) {
		return dataType == DT_Map;
	}


	private static Byte convertToByte(int srcType, Object value) {
		switch (srcType) {
			case DT_Short:
				return ((Short) value).byteValue();
			case DT_Integer:
				return ((Integer) value).byteValue();
			case DT_Long:
				return ((Long) value).byteValue();
			case DT_BigInteger:
				return ((BigInteger) value).byteValue();
			case DT_Float:
				return ((Float) value).byteValue();
			case DT_Double:
				return ((Double) value).byteValue();
			case DT_BigDecimal:
				return ((BigDecimal) value).byteValue();
			case DT_Character:
				return Byte.parseByte(((Character) value).toString());
			case DT_String:
				return Byte.parseByte((String) value);
			case DT_Boolean:
				return (byte) ((Boolean) value ? 1 : 0);
			default:
				log.error("can't convert srcType:" + srcType + " to Byte for value:" + value);
				return null;
		}
	}

	private static Short convertToShort(int srcType, Object value) {
		switch (srcType) {
			case DT_Byte:
				return ((Byte) value).shortValue();
			case DT_Integer:
				return ((Integer) value).shortValue();
			case DT_Long:
				return ((Long) value).shortValue();
			case DT_BigInteger:
				return ((BigInteger) value).shortValue();
			case DT_Float:
				return ((Float) value).shortValue();
			case DT_Double:
				return ((Double) value).shortValue();
			case DT_BigDecimal:
				return ((BigDecimal) value).shortValue();
			case DT_Character:
				return Short.parseShort(((Character) value).toString());
			case DT_String:
				return Short.parseShort((String) value);
			case DT_Boolean:
				return (short) ((Boolean) value ? 1 : 0);
			default:
				log.error("can't convert srcType:" + srcType + " to Short for value:" + value);
				return null;
		}
	}

	private static LocalDate convert2LocalDate(int srcType, Object value) {
		switch (srcType) {
			case DT_String:
				try {
					return LocalDate.parse(value.toString(), yyyyMMdd);
				} catch (DateTimeParseException dept) {
					log.warn("can't convert value " + value + " to yyyy-MM-dd HH:mm:ss.SSS");
				}
				return null;
			case DT_Long:
			case DT_long:
				Date dateLong = new Date((Long) value);
				Calendar c1 = Calendar.getInstance();
				c1.setTime(dateLong);
				return LocalDate.of(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH));
			case DT_BigInteger:
				Date date = new Date(((BigInteger) value).longValue());
				Calendar c2 = Calendar.getInstance();
				c2.setTime(date);
				return LocalDate.of(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH));
			default:
				log.error("can't convert srcType:" + srcType + " to LocalDate for value:" + value);
				return null;
		}
	}

	private static LocalDateTime convert2LocalDateTime(int srcType, Object value) {
		switch (srcType) {
			case DT_String:
				try {
					return LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_DATE_TIME);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), yyyyMMddHHmmssSSS);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), yyyyMMddHHmmssSS);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), yyyyMMddHHmmssS);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), yyyyMMddHHmmss);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), yyyyMMddHHmm);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), yyyyMMddHH);
				} catch (DateTimeParseException ignored) {
				}
				try {
					return LocalDateTime.parse(value.toString(), yyyyMMdd);
				} catch (DateTimeParseException ignored) {
				}
				log.warn("can't convert value " + value + " to [yyyy-MM-dd HH:mm:ss.SSS,yyyy-MM-dd HH:mm:ss.SS,yyyy-MM-dd HH:mm:ss.S,yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm,yyyy-MM-dd HH,yyyy-MM-dd]");
				return null;
			case DT_Long:
			case DT_long:
				return LocalDateUtils.convertDateToLDT(new Date((Long) value));
			case DT_BigInteger:
				Date date = new Date(((BigInteger) value).longValue());
				Calendar c2 = Calendar.getInstance();
				c2.setTime(date);
				return LocalDateTime.of(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH),
						c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE), c2.get(Calendar.SECOND));
			default:
				log.error("can't convert srcType:" + srcType + " to LocalDate for value:" + value);
				return null;
		}
	}
}
