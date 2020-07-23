package org.loed.framework.common.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;

/**
 * 字符串处理帮助类
 *
 * @author Thomason
 * @version 1.0
 * @since 2010-04-02
 */
public class StringHelper {

	private static final Random RANDOM = new Random();

	private static final String SQL_REGEX = "('.+--)|(--)|(\\|)|(%7C)";

	public static String escapeSql(String sql) {
		if (sql == null) {
			return null;
		}
		sql = StringUtils.replace(sql, "'", "''");
		sql = StringUtils.replace(sql, "\\", "\\\\");
		return sql;
	}

	public static String formatPath(String url) {
		if (url == null) {
			return null;
		}
		return url.replaceAll("/+/", "/");
	}

	public static String camelToUnderline(String s) {
		if (s == null || "".equals(s.trim())) {
			return ""
					;
		}
		int len = s.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append("_");
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String underlineToCamel(String s) {
		String[] strs = s.toLowerCase().split("_");
		StringBuilder result = new StringBuilder();
		String preStr = "";
		for (int i = 0; i < strs.length; i++) {
			if (preStr.length() == 1) {
				result.append(strs[i]);
			} else {
				result.append(StringUtils.capitalize(strs[i]));
			}
			preStr = strs[i];
		}
		return result.toString();
	}

	public static String replace(String inString, String oldPattern, String newPattern) {
		if (inString == null) {
			return null;
		}
		if (oldPattern == null || newPattern == null) {
			return inString;
		}

		StringBuilder builder = new StringBuilder();
		// output StringBuffer we'll build up
		int pos = 0; // our position in the old string
		int index = inString.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			builder.append(inString.substring(pos, index));
			builder.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}
		builder.append(inString.substring(pos));

		// remember to append any characters to the right of a match
		return builder.toString();
	}

//	public static String capitalize(String str) {
//		return changeFirstCharacterCase(str, true);
//	}
//
//	public static String uncapitalize(String str) {
//		return changeFirstCharacterCase(str, false);
//	}
//
//	private static String changeFirstCharacterCase(String str, boolean capitalize) {
//		if (str == null || str.length() == 0) {
//			return str;
//		}
//		StringBuilder buf = new StringBuilder(str.length());
//		if (capitalize) {
//			buf.append(Character.toUpperCase(str.charAt(0)));
//		} else {
//			buf.append(Character.toLowerCase(str.charAt(0)));
//		}
//		buf.append(str.substring(1));
//		return buf.toString();
//	}

	/**
	 * Takes care of the fact that Sun changed the output of
	 * BigDecimal.toString() between JDK-1.4 and JDK 5
	 *
	 * @param decimal the big decimal to stringify
	 * @return a string representation of 'decimal'
	 */
	public static String consistentToString(BigDecimal decimal) {
		if (decimal == null) {
			return null;
		}
		return decimal.toString();
	}

	public static String random(int count, boolean letters, boolean numbers) {
		return random(count, 0, 0, letters, numbers);
	}

	public static String random(int count, int start, int end, boolean letters,
	                            boolean numbers) {
		return random(count, start, end, letters, numbers, null, RANDOM);
	}

	public static String random(int count, int start, int end, boolean letters,
	                            boolean numbers, char[] chars, Random random) {
		if (count == 0) {
			return "";
		} else if (count < 0) {
			throw new IllegalArgumentException(
					"Requested random string length " + count
							+ " is less than 0.");
		}
		if ((start == 0) && (end == 0)) {
			end = 'z' + 1;
			start = ' ';
			if (!letters && !numbers) {
				start = 0;
				end = Integer.MAX_VALUE;
			}
		}

		char[] buffer = new char[count];
		int gap = end - start;

		while (count-- != 0) {
			char ch;
			if (chars == null) {
				ch = (char) (random.nextInt(gap) + start);
			} else {
				ch = chars[random.nextInt(gap) + start];
			}
			if ((letters && Character.isLetter(ch))
					|| (numbers && Character.isDigit(ch))
					|| (!letters && !numbers)) {
				if (ch >= 56320 && ch <= 57343) {
					if (count == 0) {
						count++;
					} else {
						// low surrogate, insert high surrogate after putting it
						// in
						buffer[count] = ch;
						count--;
						buffer[count] = (char) (55296 + random.nextInt(128));
					}
				} else if (ch >= 55296 && ch <= 56191) {
					if (count == 0) {
						count++;
					} else {
						// high surrogate, insert low surrogate before putting
						// it in
						buffer[count] = (char) (56320 + random.nextInt(128));
						count--;
						buffer[count] = ch;
					}
				} else if (ch >= 56192 && ch <= 56319) {
					// private high surrogate, no effing clue, so skip it
					count++;
				} else {
					buffer[count] = ch;
				}
			} else {
				count++;
			}
		}
		return new String(buffer);
	}

	public static boolean isEmpty(String str) {
		return StringUtils.isEmpty(str);
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 重写split方法，返回指定index的字符串
	 *
	 * @param str
	 * @param regx
	 * @param index
	 * @return
	 */
	public static String split(String str, String regx, int index) {
		if (str == null || regx == null) {
			return null;
		}
		int splitIndex = 0;
		int counter = 0;
		int beginIndex = 0;
		while ((splitIndex = str.indexOf(regx, splitIndex)) != -1) {
			counter++;
			if (counter + 1 == index) {
				beginIndex = splitIndex;
			}
			if (counter == index) {
				return str.substring(beginIndex + 1, splitIndex);
			}
			splitIndex++;
		}
		return null;
	}

	/**
	 * Adds '+' to decimal numbers that are positive (MySQL doesn't understand
	 * them otherwise
	 *
	 * @param dString The value as a string
	 * @return String the string with a '+' added (if needed)
	 */
	public static String fixDecimalExponent(String dString) {
		int ePos = dString.indexOf('E');

		if (ePos == -1) {
			ePos = dString.indexOf('e');
		}

		if (ePos != -1) {
			if (dString.length() > (ePos + 1)) {
				char maybeMinusChar = dString.charAt(ePos + 1);

				if (maybeMinusChar != '-' && maybeMinusChar != '+') {
					StringBuilder strBuilder = new StringBuilder(dString.length() + 1);
					strBuilder.append(dString.substring(0, ePos + 1));
					strBuilder.append('+');
					strBuilder.append(dString.substring(ePos + 1, dString.length()));
					dString = strBuilder.toString();
				}
			}
		}

		return dString;
	}

	/**
	 * 将对象转换成字符串，如果是日期格式的字符串，做些特殊处理
	 *
	 * @param obj 对象
	 * @return 字符串
	 */
	public static String obj2Str(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
			return sdf.format((Date) obj);
		}
		return obj.toString();
	}

	/**
	 * 取得制定长度的不重复的字符串
	 *
	 * @param length 随即字符串的长度
	 * @return
	 */
	public static String getRandomString(int length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int random = (int) (Math.random() * 1000) % 26;
			char c = getRandomChar(random);
			builder.append(c);
		}
		return builder.toString();
	}

	/**
	 * 获取随机字符
	 *
	 * @param random
	 * @return
	 */
	private static char getRandomChar(int random) {
		char c;
		switch (random) {
			case 0:
				c = 'a';
				break;
			case 1:
				c = 'b';
				break;
			case 2:
				c = 'c';
				break;
			case 3:
				c = 'd';
				break;
			case 4:
				c = 'e';
				break;
			case 5:
				c = 'f';
				break;
			case 6:
				c = 'g';
				break;
			case 7:
				c = 'h';
				break;
			case 8:
				c = 'i';
				break;
			case 9:
				c = 'j';
				break;
			case 10:
				c = 'k';
				break;
			case 11:
				c = 'l';
				break;
			case 12:
				c = 'm';
				break;
			case 13:
				c = 'n';
				break;
			case 14:
				c = 'o';
				break;
			case 15:
				c = 'p';
				break;
			case 16:
				c = 'q';
				break;
			case 17:
				c = 'r';
				break;
			case 18:
				c = 's';
				break;
			case 19:
				c = 't';
				break;
			case 20:
				c = 'u';
				break;
			case 21:
				c = 'v';
				break;
			case 22:
				c = 'w';
				break;
			case 23:
				c = 'x';
				break;
			case 24:
				c = 'y';
				break;
			case 25:
				c = 'z';
				break;
			default:
				//不可能出现的情况
				c = '&';
				break;
		}
		return c;
	}


	/**
	 * 取得制定长度的不重复的字符串
	 *
	 * @return
	 */
	public static String getRandomString() {
		return getRandomString(5);
	}

	/**
	 * 将string的指定字符去掉
	 *
	 * @param src 源字符串
	 * @param rem 指定字符串
	 * @return 处理后的字符串
	 */
	public static String trimString(String src, String rem) {
		if (src == null) {
			return null;
		}
		if ("".equals(src)) {
			return src;
		}
		StringBuilder sb = new StringBuilder(src);
		while (rem.contains(Character.valueOf(sb.charAt(0)).toString())) {
			sb.deleteCharAt(0);
		}
		while (rem.contains(Character.valueOf(sb.charAt(sb.length() - 1)).toString())) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * 判断包含
	 *
	 * @param urlSet 授权url集合
	 * @param url    请求url
	 * @return 是否匹配
	 */
	public static boolean contains(Set<String> urlSet, String url) {
		boolean flag = false;
		final String path = StringHelper.formatPath(url);
		for (String s : urlSet) {
			if (StringHelper.simpleWildcardMatch(s, path)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * 通配符匹配
	 *
	 * @param pattern 模式
	 * @param str     字符串
	 * @return 是否匹配
	 */
	public static boolean simpleWildcardMatch(String pattern, String str) {
		return wildcardMatch(pattern, str, "*");
	}

	/**
	 * 通配符匹配
	 *
	 * @param pattern  模式
	 * @param str      字符
	 * @param wildcard 通配符
	 * @return 是否匹配
	 */
	public static boolean wildcardMatch(String pattern, String str, String wildcard) {
		if (StringUtils.isEmpty(pattern) || StringUtils.isEmpty(str)) {
			return false;
		}
		final boolean startWith = pattern.startsWith(wildcard);
		final boolean endWith = pattern.endsWith(wildcard);
		String[] array = StringUtils.split(pattern, wildcard);
		int currentIndex = -1;
		int lastIndex = -1;
		switch (array.length) {
			case 0:
				return true;
			case 1:
				currentIndex = str.indexOf(array[0]);
				if (startWith && endWith) {
					return currentIndex >= 0;
				}
				if (startWith) {
					return currentIndex + array[0].length() == str.length();
				}
				if (endWith) {
					return currentIndex == 0;
				}
				return str.equals(pattern);
			default:
				for (String part : array) {
					currentIndex = str.indexOf(part);
					if (currentIndex > lastIndex) {
						lastIndex = currentIndex;
						continue;
					}
					return false;
				}
				return true;
		}
	}
}
