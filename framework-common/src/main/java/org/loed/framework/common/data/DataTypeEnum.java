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

	public static DataTypeEnum of(Class<?> clazz) {
		if (clazz.isArray()) {
			return DT_Array;
		}
		if (clazz.isEnum()) {
			return DT_Enum;
		}
		String name = clazz.getName();


		return DT_UserDefine;
	}

	public static DataTypeEnum from(Object value) {
		if (value == null) {
			return DT_Unknown;
		}
		return of(value.getClass());
	}

	public static boolean isSimpleType(DataTypeEnum type) {
		return
				type == DT_byte ||
						type == DT_short ||
						type == DT_int ||
						type == DT_long ||
						type == DT_float ||
						type == DT_double ||
						type == DT_char ||
						type == DT_boolean ||
						type == DT_Byte ||
						type == DT_Short ||
						type == DT_Integer ||
						type == DT_Long ||
						type == DT_Float ||
						type == DT_Double ||
						type == DT_Character ||
						type == DT_Boolean ||
						type == DT_String ||
						type == DT_BigInteger ||
						type == DT_BigDecimal ||
						type == DT_Date ||
						type == DT_SqlDate ||
						type == DT_SqlTime ||
						type == DT_SqlTimestamp ||
						type == DT_LocalDate ||
						type == DT_LocalDateTime ||
						type == DT_ZoneDateTime ||
						type == DT_SqlClob ||
						type == DT_SqlBlob;
	}

	/**
	 * 判断对象类型是否一致，支持装箱(boxing)和拆箱(unboxing)类型的对比
	 *
	 * @param orig 原始数据类型
	 * @param dest 目标数据类型
	 * @return 是否一致
	 */
	public static boolean isSameType(DataTypeEnum orig, DataTypeEnum dest) {
		if (orig == dest) {
			return true;
		}
		if ((orig == DT_Boolean && dest == DT_boolean)
				|| (orig == DT_boolean && dest == DT_Boolean)) {
			return true;
		}
		return false;
	}
}
