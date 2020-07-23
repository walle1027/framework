package org.loed.framework.common;//package org.loed.framework.common;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//
//import java.io.*;
//import java.net.URL;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
///**
// * 系统配置类，主要用于对系统使用的多种实现的一些配置
// * 比如，i18n消息是从redis取，还是从配置文件取，甚至是从数据库取
// * 登录校验方式是通过session还是cookie
// * ioc框架是srping还是ejb等等
// *
// * @author Thomason
// * @version 1.0
// * @since 12-4-1 下午10:30
// */
//public class SystemConfig {
//	public static final String I18N_PROVIDER_PROPERTY = "property";
//	public static final String I18N_PROVIDER_CACHE = "cache";
//	public static final String I18N_PROVIDER_DB = "db";
//	public static final String CACHE_PROVIDER_HASHMAP = "hashMap";
//	public static final String UI_VERSION_KEY = "__uiVersion";
//	public static final String DEFAULT_UI_VERSION = "dojo";
//	/**
//	 * i18n提供者的key
//	 */
//	private static final String I18N_PROVIDER_KEY = "__i18nProvider";
//	private static final String CACHE_PROVIDER_KEY = "__cacheProvider";
//	private static final String CACHE_PROVIDER_REDIS = "redis";
//	private static SystemConfig _instance = new SystemConfig();
//	private static Logger logger = org.slf4j.LoggerFactory.getLogger(SystemConfig.class);
//	private static Map<String, String> configMap = new ConcurrentHashMap<String, String>();
//	private static Map<String, String> configKeyFileMap = new ConcurrentHashMap<String, String>();
//	private static Map<String, Long> configFileMap = new ConcurrentHashMap<String, Long>();
//	private static String MOCK_FILE_PATH = "MOCK_FILE_PATH";
//	private static Lock lock = new ReentrantLock();
//
//	/**
//	 * 私有的构造方法，确保唯一性
//	 */
//	private SystemConfig() {
//	}
//
//	@Deprecated
//	public static SystemConfig getInstance() {
//		return _instance;
//	}
//
//	public static String i18nProvider() {
//		String i18nProvider = get(I18N_PROVIDER_KEY);
//		if (StringUtils.isBlank(i18nProvider)) {
//			return I18N_PROVIDER_CACHE;
//		}
//		return i18nProvider;
//	}
//
//	public static String cacheProvider() {
//		String cacheProvider = get(CACHE_PROVIDER_KEY);
//		if (StringUtils.isBlank(cacheProvider)) {
//			return CACHE_PROVIDER_REDIS;
//		}
//		return cacheProvider;
//	}
//
//	public static boolean useClasspathPage() {
//		return true;
//	}
//
//	public static String getConfig(String configKey) {
//		return get(configKey);
//	}
//
//	public static void setConfig(String propKey, String propValue) {
//		configKeyFileMap.put(propKey, MOCK_FILE_PATH);
//		configMap.put(propKey, propValue);
//	}
//
//	public static Map<String, String> getAllProps() {
//		return configMap;
//	}
//
//	public static String getConfig(String configKey, String defaultValue) {
//		String value = getConfig(configKey);
//		if (StringUtils.isBlank(value)) {
//			return defaultValue;
//		}
//		return value;
//	}
//
//	/**
//	 * 取得经过RSA解密后的字符串
//	 *
//	 * @param configKey    配置Key
//	 * @param defaultValue 默认值
//	 * @return 解密后的字符
//	 */
//	public static String getEncryptedConfig(String configKey, String defaultValue) {
//		String encryptedConfig = getEncryptedConfig(configKey);
//		if (StringUtils.isBlank(encryptedConfig)) {
//			return defaultValue;
//		}
//		return encryptedConfig;
//	}
//
//	/**
//	 * 取得经过RSA加密的配置参数
//	 * 需要先在系统中配置rsa.privateKey 即rsa的私钥
//	 * 然后根据私钥进行解密
//	 *
//	 * @param configKey 配置Key
//	 * @return 解密后的字符
//	 */
//	public static String getEncryptedConfig(String configKey) {
//		/*try {
//			String configValue = getConfig(configKey);
//			if (StringUtils.isBlank(configValue)) {
//				return configValue;
//			}
//			String keyStr = getConfig("rsa.privateKey");
//			byte[] decrypt = RSAUtils.decryptByPrivateKey(Hex.decodeHex(configValue.toCharArray()), keyStr);
//			return Hex.encodeHexString(decrypt);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}*/
//		return null;
//	}
//
//	private static String get(String prop) {
//		String filePath = configKeyFileMap.get(prop);
//		if (MOCK_FILE_PATH.equals(filePath)) {
//			return configMap.get(prop);
//		}
//		if (filePath == null) {
//			return null;
//		}
//		long lastModified = getFileLastModify(filePath);
//		if (lastModified > configFileMap.get(filePath)) {
//			loadConfigFile(filePath);
//		}
//		return configMap.get(prop);
//	}
//
//
//	public static void addClassPathConfigFiles(String... configFilePath) {
//		for (String filePath : configFilePath) {
//			loadClassPathConfigFile(filePath);
//		}
//	}
//
//	public static void addConfigFiles(String... configFilePath) {
//		for (String filePath : configFilePath) {
//			loadConfigFile(filePath);
//		}
//	}
//
//	private static void loadConfigFile(String filePath) {
//		InputStream inputStream = null;
//		InputStreamReader reader = null;
//		try {
//			Properties properties = new Properties();
//			File configFile = new File(filePath);
//			String absolutePath = configFile.getAbsolutePath();
//			configFileMap.put(absolutePath, configFile.lastModified());
//			inputStream = new FileInputStream(configFile);
//			reader = new InputStreamReader(inputStream, "UTF-8");
//			properties.load(reader);
//			for (Map.Entry<Object, Object> entry : properties.entrySet()) {
//				String key = entry.getKey() + "";
//				String value = entry.getValue() + "";
//				configKeyFileMap.put(key, absolutePath);
//				configMap.put(key, value);
//			}
//		} catch (Exception e) {
//			logger.error("read config file occur error: " + e.getMessage());
//		} finally {
//			if (inputStream != null) {
//				try {
//					inputStream.close();
//				} catch (IOException e) {
//					logger.error(e.getMessage(), e);
//				}
//			}
//			if (reader != null) {
//				try {
//					reader.close();
//				} catch (IOException e) {
//					logger.error(e.getMessage(), e);
//				}
//			}
//		}
//	}
//
//	private static void loadClassPathConfigFile(String filePath) {
//		URL url = SystemConfig.class.getClassLoader().getResource(filePath);
//		if (url != null) {
//			loadConfigFile(url.getFile());
//		}
//	}
//
//	private static long getFileLastModify(String filePath) {
//		File configFile = new File(filePath);
//		return configFile.lastModified();
//	}
//}
