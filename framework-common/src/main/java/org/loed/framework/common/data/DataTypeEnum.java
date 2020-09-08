package org.loed.framework.common.data;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/8 4:20 下午
 */
public enum DataTypeEnum {
	/**
	 * 未知类型
	 */
	DT_Unknown,
	/**
	 * 简单类型
	 */
	DT_byte,
	DT_short,
	DT_int,
	DT_long,
	DT_float,
	DT_double,
	DT_char,
	DT_boolean,
	DT_Byte,
	DT_Short,
	DT_Integer,
	DT_Long,
	DT_Float,
	DT_Double,
	DT_Character,
	DT_Boolean,
	DT_String,
	DT_BigInteger,
	DT_BigDecimal,
	DT_Date,
	DT_SqlDate,
	DT_SqlTime,
	DT_SqlTimestamp,
	DT_LocalDate,
	DT_LocalDateTime,
	DT_ZoneDateTime,
	DT_SqlClob,
	DT_SqlBlob,

	/**
	 * collection types
	 */
	DT_Array,
	DT_List,
	DT_Map,
	DT_Set,

	/**
	 * object types
	 */
	DT_Object,
	DT_Class,
	DT_Enum,
	DT_UserDefine;
}
