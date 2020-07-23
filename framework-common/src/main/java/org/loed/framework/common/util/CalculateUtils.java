package org.loed.framework.common.util;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created with IntelliJ IDEA.
 * User: HAL
 * Date: 12-7-1
 * Time: 上午1:01
 * To change this template use File | Settings | File Templates.
 */
public class CalculateUtils {

	/**
	 * 相加后返回结果arg1+arg2
	 *
	 * @param arg1 参数1
	 * @param arg2 参数2
	 * @return 和
	 */
	public static BigDecimal add(BigDecimal arg1, BigDecimal arg2) {
		arg1 = null2Zero(arg1);
		arg2 = null2Zero(arg2);
		BigDecimal result = arg1.add(arg2, MathContext.UNLIMITED);
		return formatResult(result);
	}

	/**
	 * 相减后返回结果arg1-arg2
	 *
	 * @param arg1 参数1
	 * @param arg2 参数2
	 * @return 差
	 */
	public static BigDecimal subtract(BigDecimal arg1, BigDecimal arg2) {
		arg1 = null2Zero(arg1);
		arg2 = null2Zero(arg2);
		BigDecimal result = arg1.subtract(arg2, MathContext.UNLIMITED);
		return formatResult(result);
	}

	/**
	 * 相乘后返回结果arg1*arg2
	 *
	 * @param arg1 参数1
	 * @param arg2 参数2
	 * @return 积
	 */
	public static BigDecimal multiply(BigDecimal arg1, BigDecimal arg2) {
		arg1 = null2Zero(arg1);
		arg2 = null2Zero(arg2);
		BigDecimal result = arg1.multiply(arg2, MathContext.UNLIMITED);
		return formatResult(result);
	}

	/**
	 * 相除后返回结果arg1/arg2
	 *
	 * @param arg1 参数1
	 * @param arg2 参数2
	 * @return 商
	 */
	public static BigDecimal divide(BigDecimal arg1, BigDecimal arg2) {
		arg1 = null2Zero(arg1);
		BigDecimal result = arg1.divide(arg2, MathContext.UNLIMITED);
		return formatResult(result);
	}

	/**
	 * 空值校验，如为null返回0
	 *
	 * @param arg 参数
	 * @return 值
	 */
	public static BigDecimal null2Zero(BigDecimal arg) {
		return arg == null ? BigDecimal.ZERO : arg;
	}

	private static BigDecimal formatResult(BigDecimal result) {
		return result.setScale(8, BigDecimal.ROUND_HALF_UP);
	}
}
