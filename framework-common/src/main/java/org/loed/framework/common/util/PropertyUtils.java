package org.loed.framework.common.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Hashtable;
import java.util.Properties;

/**
 * 功能描述:动态读取配置文件来加载属性
 *
 * @author 杨涛
 * @since 2009/10/9 version(2.0)
 */
public class PropertyUtils {

	private static Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

	/**
	 * 方法用途和描述: 获得属性
	 *
	 * @param propertyFilePath 属性文件(包括类路径)
	 * @param key              属性键
	 * @return 属性值
	 * @since wapportal_manager version(2.0)
	 */
	public static String getValue(String propertyFilePath, String key) {
		Properties ppts = getProperties(propertyFilePath);
		return ppts == null ? null : ppts.getProperty(key);
	}

	/**
	 * 方法用途和描述: 获得属性文件中Key所对应的值
	 *
	 * @param propertyFilePath 属性文件路径(包括类路径或文件系统中文件路径)
	 * @param key              属性的键
	 * @param isAbsolutePath   是否为绝对路径:true|false〔即是本地文件系统路径，比如：C:/test.propreties〕<br>
	 *                         <br>
	 *                         <b>注：</b>不能通过类路径来获取到属性文件，而只知道属性文件的文件系统路径，即文件系统地址则用此方法来获取其中的Key所对应的Value
	 * @return key的属性值
	 * @since wapportal_manager version(2.0)
	 */
	public static String getValue(String propertyFilePath, String key,
	                              boolean isAbsolutePath) {
		if (isAbsolutePath) {
			Properties ppts = getPropertiesByFs(propertyFilePath);
			return ppts == null ? null : ppts.getProperty(key);
		}
		return getValue(propertyFilePath, key);
	}

	/**
	 * 方法用途和描述: 获得属性文件的属性
	 *
	 * @param propertyFilePath 属性文件(包括类路径)
	 * @return 属性
	 * @since wapportal_manager version(2.0)
	 */
	public static Properties getProperties(String propertyFilePath) {
		if (propertyFilePath == null) {
			logger.error("propertyFilePath is null!");
			return null;
		}
		return loadPropertyFile(propertyFilePath);
	}

	/**
	 * 方法用途和描述: 获得属性文件的属性
	 *
	 * @param propertyFilePath 属性文件路径(包括类路径及文件系统路径)
	 * @return 属性文件对象 Properties
	 * @since wapportal_manager version(2.0)
	 */
	public static Properties getPropertiesByFs(String propertyFilePath) {
		if (propertyFilePath == null) {
			logger.error("propertyFilePath is null!");
			return null;
		}
		return loadPropertyFileByFileSystem(propertyFilePath);
	}

	/**
	 * 方法用途和描述: 加载属性
	 *
	 * @param propertyFilePath 属性文件(包括类路径)
	 * @return 属性
	 * @since wapportal_manager version(2.0)
	 */
	private static Properties loadPropertyFile(String propertyFilePath) {
		InputStream is = PropertyUtils.class.getClassLoader()
				.getResourceAsStream(propertyFilePath);
		if (is == null) {
			return loadPropertyFileByFileSystem(propertyFilePath);
		}
		Properties ppts = new Properties();
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(is, "utf-8");
			ppts.load(is);
			return ppts;
		} catch (Exception e) {
			logger.debug("加载属性文件出错:" + propertyFilePath, e);
			return null;
		} finally {
			try {
				is.close();
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

	/**
	 * 方法用途和描述: 从文件系统加载属性文件
	 *
	 * @param propertyFilePath 属性文件(文件系统的文件路径)
	 * @return 属性
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	private static Properties loadPropertyFileByFileSystem(
			final String propertyFilePath) {
		InputStreamReader reader = null;
		FileInputStream inputStream = null;
		try {
			Properties ppts = new Properties();
			inputStream = new FileInputStream(propertyFilePath);
			reader = new InputStreamReader(inputStream, "utf-8");
			ppts.load(reader);
			return ppts;
		} catch (FileNotFoundException e) {
			logger.error("FileInputStream(\"" + propertyFilePath
					+ "\")! FileNotFoundException: " + e);
			return null;
		} catch (IOException e) {
			logger.error("Properties.load(InputStream)! IOException: " + e);
			return null;
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

	/**
	 * 方法用途和描述: 对存在的属性文件中添加键值对并保存
	 *
	 * @param propertyFilePath 属性文件的路径(包括类路径及文件系统路径)
	 * @param htKeyValue       键值对Hashtable
	 * @return 保存是否成功
	 * @since wapportal_manager version(2.0)
	 */
	public static boolean setValueAndStore(String propertyFilePath,
	                                       Hashtable<String, String> htKeyValue) {
		return setValueAndStore(propertyFilePath, htKeyValue, null);
	}

	/**
	 * 方法用途和描述: 对存在的属性文件中添加键值对并保存
	 *
	 * @param propertyFilePath 属性文件的路径(包括类路径及文件系统路径)
	 * @param htKeyValue       键值对Hashtable
	 * @param storeMsg         保存时添加的附加信息（注释）
	 * @return 保存是否成功
	 * @since wapportal_manager version(2.0)
	 */
	public static boolean setValueAndStore(String propertyFilePath,
	                                       Hashtable<String, String> htKeyValue, String storeMsg) {
		Properties ppts = getProperties(propertyFilePath);

		if (ppts == null || htKeyValue == null) {
			return false;
		}
		ppts.putAll(htKeyValue);
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(propertyFilePath), "UTF-8");
		} catch (FileNotFoundException e) {
			logger.debug("propertyFilePath = " + propertyFilePath);
			String path = PropertyUtils.class.getResource(propertyFilePath)
					.getPath();
			logger.debug("~~~~~~~~path~~~XXX~~~~~" + path);
			try {
				writer = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
			} catch (FileNotFoundException e1) {
				logger.error("FileNotFoundException! path=" + propertyFilePath);
				return false;
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				logger.error(e1.getMessage(), e1);
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		if (writer == null) {
			return false;
		}

		try {
			ppts.store(writer, storeMsg != null ? storeMsg
					: "set value and store.");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 方法用途和描述: 创建属性文件
	 *
	 * @param propertyFilePath 要存储属性文件的路径
	 * @param htKeyValue       属性文件中的键值对Hashtable
	 * @return 是否成功
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	public static boolean createPropertiesFile(String propertyFilePath,
	                                           Hashtable<String, String> htKeyValue) {
		return createPropertiesFile(new File(propertyFilePath), htKeyValue);
	}

	/**
	 * 方法用途和描述: 创建属性文件
	 *
	 * @param file       要存储属性文件
	 * @param htKeyValue 属性文件中的键值对Hashtable
	 * @return 是否成功
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	public static boolean createPropertiesFile(File file, Hashtable<String, String> htKeyValue) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return setValueAndStore(file.getAbsolutePath(), htKeyValue,
				"create properties file:" + file.getName());
	}

	/**
	 * 方法用途和描述:设置属性值
	 *
	 * @param propertyFilePath 属性文件(包括类路径)
	 * @param key              属性键
	 * @param value            属性值
	 * @return
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	public static boolean setValue(String propertyFilePath, String key,
	                               String value) {
		Properties ppts = getProperties(propertyFilePath);
		if (ppts == null) {
			return false;
		}
		ppts.put(key, value);
		return true;
	}

	/**
	 * 方法用途和描述: 保存属性文件对象
	 *
	 * @param properties       属性文件对象
	 * @param propertyFilePath 要保存的路径
	 * @param msg              保存时添加的附加信息（注释）
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	public final static void store(Properties properties,
	                               String propertyFilePath, String msg) {
		try {
			OutputStream stream = new FileOutputStream(
					propertyFilePath);
			properties.store(stream, msg);
		} catch (FileNotFoundException e) {
			logger.error("FileOutputStream(" + propertyFilePath
					+ ")! FileNotFoundException: " + e);
		} catch (IOException e) {
			logger.error("store(stream, msg)! IOException: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * 方法用途和描述: 删除属性值
	 *
	 * @param propertyFilePath 属性文件(包括类路径)
	 * @param key              属性键
	 * @return
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	public static String removeValue(String propertyFilePath, String key) {
		Properties ppts = getProperties(propertyFilePath);
		if (ppts == null) {
			return null;
		}
		return (String) ppts.remove(key);
	}

	/**
	 * 方法用途和描述: 删除属性文件中的Key数组所对应的键值对
	 *
	 * @param propertyFilePath 属性文件路径(包括类路径及文件系统路径)
	 * @param key              key数组
	 * @return 属性文件对象
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	public final static Properties removeValue(String propertyFilePath,
	                                           String[] key) {
		if (key == null) {
			logger.error("key[] is null!");
			return null;
		}
		Properties ppts = getProperties(propertyFilePath);
		if (ppts == null) {
			return null;
		}
		for (String strKey : key) {
			ppts.remove(strKey);
		}
		return ppts;
	}

	/**
	 * 方法用途和描述:删除属性文件中的Key数组所对应的键值对，并将属性文件对象持久化（即保存）
	 *
	 * @param propertyFilePath 属性文件路径(包括类路径及文件系统路径)
	 * @param key              属性文件中的key数组
	 * @return 成功与否（true|false）
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	public final static boolean removeValueAndStore(String propertyFilePath,
	                                                String[] key) {
		Properties ppts = removeValue(propertyFilePath, key);
		if (ppts == null) {
			return false;
		}
		store(ppts, propertyFilePath, "batch remove key value!");
		return true;
	}

	/**
	 * 方法用途和描述: 更新指定路径的属性文件中的键所对应的值
	 *
	 * @param propertyFilePath 属性文件路径(包括类路径及文件系统路径)
	 * @param key              属性文件中的key
	 * @param newValue         要更新的新值
	 * @return 成功与否（true|false）
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	public final static boolean updateValue(String propertyFilePath,
	                                        String key, String newValue) {
		if (key == null || newValue == null) {
			logger.error("key or newValue is null!");
			return false;
		}
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put(key, newValue);
		return setValueAndStore(propertyFilePath, ht, "update " + key
				+ "'s value!");
	}

	/**
	 * 方法用途和描述: 批量更新指定路径的属性文件中的键所对应的值
	 *
	 * @param propertyFilePath 属性文件路径(包括类路径及文件系统路径)
	 * @param htKeyValue       要更新的键值对Hashtable
	 * @return 成功与否（true|false）
	 * @author dengcd 新增日期：2008-10-9
	 * @since wapportal_manager version(2.0)
	 */
	public static boolean batchUpdateValue(String propertyFilePath,
	                                       Hashtable<String, String> htKeyValue) {
		if (propertyFilePath == null || htKeyValue == null) {
			return false;
		}
		return setValueAndStore(propertyFilePath, htKeyValue,
				"batch update key value!");
	}
}
