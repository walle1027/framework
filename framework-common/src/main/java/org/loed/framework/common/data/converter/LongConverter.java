package org.loed.framework.common.data.converter;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.data.DataConvertException;
import org.loed.framework.common.data.DataTypeEnum;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/2 10:27 上午
 */
@SuppressWarnings("DuplicateBranchesInSwitch")
@Slf4j
public class LongConverter extends SimpleTypeConverter<Long> {
	@Override
	public Long convert(Object origin) {
		DataTypeEnum originType = DataTypeEnum.from(origin);
		try {
			switch (originType) {
				case DT_byte:
					return Long.valueOf(String.valueOf(origin));
				case DT_short:
					return Short.valueOf((short) origin).longValue();
				case DT_int:
					return Integer.valueOf((int) origin).longValue();
				case DT_long:
					return (long) origin;
				case DT_float:
					return Float.valueOf((float) origin).longValue();
				case DT_double:
					return Double.valueOf((double) origin).longValue();
				case DT_char:
					return Long.valueOf(Character.valueOf((char) origin).toString());
				case DT_boolean:
					boolean bool = (boolean) origin;
					if (bool) {
						return 1L;
					} else {
						return 0L;
					}
				case DT_Byte:
					return ((Byte) origin).longValue();
				case DT_Short:
					return ((Short) origin).longValue();
				case DT_Integer:
					return ((Integer) origin).longValue();
				case DT_Long:
					return (Long) origin;
				case DT_Float:
					return ((Float) origin).longValue();
				case DT_Double:
					return ((Double) origin).longValue();
				case DT_Character:
					return Long.parseLong(String.valueOf(origin));
				case DT_Boolean:
					Boolean boolValue = (Boolean) origin;
					if (boolValue) {
						return 1L;
					} else {
						return 0L;
					}
				case DT_String:
					return Long.valueOf(String.valueOf(origin));
				case DT_BigInteger:
					return ((BigInteger) origin).longValue();
				case DT_BigDecimal:
					return ((BigDecimal) origin).longValue();
				case DT_Date:
					return ((Date) origin).getTime();
				case DT_SqlDate:
					return ((java.sql.Date) origin).getTime();
				case DT_SqlTime:
					return ((java.sql.Time) origin).getTime();
				case DT_SqlTimestamp:
					return ((java.sql.Timestamp) origin).getTime();
				case DT_LocalDate:
					return ((LocalDate) origin).toEpochDay();
				case DT_LocalDateTime:
					return ((LocalDateTime) origin).toEpochSecond(ZoneOffset.UTC);
				case DT_ZoneDateTime:
					return ((ZonedDateTime) origin).toEpochSecond();
				case DT_SqlClob:
					throw new RuntimeException("not supported types");
				case DT_SqlBlob:
					throw new RuntimeException("not supported types");
				default:
					throw new RuntimeException("not supported types");
			}
		} catch (Exception e) {
			throw new DataConvertException("can't convert value:" + origin + " to Long,caused by :" + e.getMessage(), e);
		}
	}
}
