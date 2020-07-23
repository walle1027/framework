package org.loed.framework.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/4/5 17:46
 */
public class PinyinUtils {
	public static Map<String, String> pinyinMap = new HashMap<>();

	static {
		InputStream inputStream = null;
		Reader reader = null;
		try {
			inputStream = PinyinUtils.class.getClassLoader().getResourceAsStream("pinyin.properties");
			reader = new InputStreamReader(inputStream, "UTF-8");
			Properties properties = new Properties();
			properties.load(reader);
			for (Map.Entry<Object, Object> entry : properties.entrySet()) {
				pinyinMap.put(entry.getKey() + "", entry.getValue() + "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getFirstPinyin(String chinese) {
		String pinyin = pinyinMap.get(chinese);
		if (pinyin != null) {
			if (pinyin.contains(",")) {
				return pinyin.substring(0, pinyin.indexOf(","));
			}
			return pinyin;
		}
		return null;
	}

	public static String getPinyinLetter(String chinese, int length) {
		String pinyin = pinyinMap.get(chinese);
		if (pinyin != null) {
			String substring;
			if (pinyin.contains(",")) {
				substring = pinyin.substring(0, pinyin.indexOf(","));
			} else {
				substring = pinyin;
			}
			if (substring.length() > length) {
				return substring.substring(0, length);
			}
			return substring;
		}
		return null;
	}
}
