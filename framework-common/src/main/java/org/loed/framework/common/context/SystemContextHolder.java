package org.loed.framework.common.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.loed.framework.common.context.SystemContext.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/12 4:41 下午
 */
public class SystemContextHolder {
	/**
	 * 用于保存线程相关信息
	 */
	private static final transient ThreadLocal<SystemContext> contextMap = new ThreadLocal<>();

	private static final Logger logger = LoggerFactory.getLogger(SystemContext.class);


	/**
	 * 从 ThreadLocal中获取名值Map(不包含appCode)
	 *
	 * @return 名值Map
	 */
	public static Map<String, String> getContextMap() {
		SystemContext systemContext = contextMap.get();
		if (systemContext == null) {
			systemContext = new SystemContext();
			contextMap.set(systemContext);
		}
		return systemContext.getContextMap();
	}

	public static SystemContext getSystemContext() {
		return contextMap.get();
	}

	/**
	 * 从 ThreadLocal 获取名值Map
	 *
	 * @param contextMap 名值Map
	 */
	public static void setContextMap(Map<String, String> contextMap) {
		getContextMap().putAll(contextMap);
	}

	/**
	 * （获取键下的值.如果不存在，返回null；如果名值Map未初始化，也返回null） Get the value of key. Would
	 * return null if context map hasn't been initialized.
	 *
	 * @param key 键
	 * @return 键下的值
	 */
	public static String get(String key) {
		Map<String, String> contextMap = getContextMap();
		if (contextMap == null) {
			return null;
		}
		return contextMap.get(key);
	}

	/**
	 * （设置名值对。如果Map之前为null，则会被初始化） set the key-value into the context map;
	 * <p/>
	 * Initialize the map if the it doesn't exist.
	 *
	 * @param key   键
	 * @param value 值
	 */
	public static void set(String key, String value) {
		if (key == null || value == null) {
			logger.error("key:" + key + " or value:" + value + " is null,i can't set it into the context map");
			return;
		}
		if (key.length() > SystemContext.MAX_SIZE) {
			throw new RuntimeException("key is more than " + SystemContext.MAX_SIZE + ", i can't set it into the context map");
		}
		if (value.length() > SystemContext.MAX_SIZE) {
			throw new RuntimeException("value is more than " + SystemContext.MAX_SIZE + ", i can't set it into the context map");
		}
		Map<String, String> contextMap = getContextMap();
		if (contextMap.size() > SystemContext.MAX_CAPACITY) {
			throw new RuntimeException("the context map is full, can't set anything");
		}
		contextMap.put(key, value);
	}

	public static String getUserToken() {
		return get(CONTEXT_USER_TOKEN);
	}


	public static void setUserToken(String token) {
		set(CONTEXT_USER_TOKEN, token);
	}


	public static String getUserId() {
		return get(CONTEXT_USER_ID);
	}


	public static void setUserId(String userId) {
		set(CONTEXT_USER_ID, userId);
	}


	public static String getUserName() {
		return get(CONTEXT_USER_NAME);
	}


	public static void setUserName(String userName) {
		set(CONTEXT_USER_NAME, userName);
	}


	public static String getUserAgent() {
		return get(CONTEXT_USER_AGENT);
	}


	public static void setUserAgent(String userAgent) {
		set(CONTEXT_USER_AGENT, userAgent);
	}


	public static String getAccountId() {
		return get(CONTEXT_ACCOUNT_ID);
	}


	public static void setAccountId(String accountId) {
		set(CONTEXT_ACCOUNT_ID, accountId);
	}


	public static String getAccountName() {
		return get(CONTEXT_ACCOUNT_NAME);
	}


	public static void setAccountName(String accountName) {
		set(CONTEXT_ACCOUNT_NAME, accountName);
	}


	public static String getAppId() {
		return get(CONTEXT_APP_ID);
	}


	public static void setAppId(String appId) {
		set(CONTEXT_APP_ID, appId);
	}


	public static String getAppVersion() {
		return get(CONTEXT_APP_VERSION);
	}


	public static void setAppVersion(String appVersion) {
		set(CONTEXT_APP_VERSION, appVersion);
	}


	public static Integer getTimeZone() {
		String timeZone = get(CONTEXT_TIME_ZONE);
		return timeZone == null ? null : Integer.parseInt(timeZone);
	}


	public static void setTimeZone(Integer timeZone) {
		set(CONTEXT_TIME_ZONE, timeZone + "");
	}


	public static String getServerHost() {
		return get(CONTEXT_SERVER_HOST);
	}


	public static void setServerHost(String serverHost) {
		set(CONTEXT_SERVER_HOST, serverHost);
	}


	public static String getPlatform() {
		return get(CONTEXT_PLATFORM);
	}


	public static void setPlatform(String device) {
		set(CONTEXT_PLATFORM, device);
	}


	public static String getDevice() {
		return get(CONTEXT_DEVICE);
	}


	public static void setDevice(String device) {
		set(CONTEXT_DEVICE, device);
	}


	public static String getTenantCode() {
		String tenantCode = get(CONTEXT_TENANT_CODE);
		if (tenantCode == null || tenantCode.isEmpty()) {
			return DEFAULT_TENANT_CODE;
		}
		return tenantCode;
	}


	public static void setTenantCode(String tenantCode) {
		set(CONTEXT_TENANT_CODE, tenantCode);
	}


	public static String getLocale() {
		String locale = get(CONTEXT_LOCALE);
		if (locale == null || locale.isEmpty()) {
			return DEFAULT_LOCALE;
		}
		return locale;
	}


	public static void setLocale(String locale) {
		set(CONTEXT_LOCALE, locale);
	}


	public static String getClientIp() {
		return get(CONTEXT_CLIENT_IP);
	}


	public static void setClientIp(String clientIp) {
		set(CONTEXT_CLIENT_IP, clientIp);
	}

	public static void clean() {
		contextMap.remove();
	}

	/**
	 * 清除指定的Key对应的元素
	 *
	 * @param key key
	 */
	public static void remove(String key) {
		getContextMap().remove(key);
	}
}
