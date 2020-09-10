package org.loed.framework.common.context;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-5 下午9:14
 */
@Slf4j
public final class SystemContext {
	public static final String DEFAULT_TENANT_ID = "default";
	public static final String DEFAULT_LOCALE = "zh_CN";
	/**
	 * 系统上下文前缀
	 */
	public static final String CONTEXT_PREFIX = "loed-context-";
	/**
	 * token
	 */
	public static final String CONTEXT_USER_TOKEN = CONTEXT_PREFIX + "userToken";
	/**
	 * 用户账号
	 */
	public static final String CONTEXT_ACCOUNT_ID = CONTEXT_PREFIX + "accountId";
	/**
	 * 用户名称
	 */
	public static final String CONTEXT_ACCOUNT_NAME = CONTEXT_PREFIX + "accountName";
	/**
	 * 用户账号
	 */
	public static final String CONTEXT_USER_ID = CONTEXT_PREFIX + "userId";
	/**
	 * 用户名称
	 */
	public static final String CONTEXT_USER_NAME = CONTEXT_PREFIX + "userName";
	/**
	 * 客户端IP
	 */
	public static final String CONTEXT_CLIENT_IP = CONTEXT_PREFIX + "clientIp";
	/**
	 * 客户端浏览信息
	 */
	public static final String CONTEXT_USER_AGENT = CONTEXT_PREFIX + "userAgent";
	/**
	 * 租户Id
	 */
	public static final String CONTEXT_TENANT_ID = CONTEXT_PREFIX + "tenantId";
	/**
	 * optional property
	 * <p>
	 * 项目Id
	 */
	public static final String CONTEXT_PROJECT_ID = CONTEXT_PREFIX + "projectId";
	/**
	 * 应用Id
	 */
	public static final String CONTEXT_APP_ID = CONTEXT_PREFIX + "appId";
	/**
	 * 应用版本号
	 */
	public static final String CONTEXT_APP_VERSION = CONTEXT_PREFIX + "appVersion";
	/**
	 * 区域和语言
	 */
	public static final String CONTEXT_LOCALE = CONTEXT_PREFIX + "locale";
	/**
	 * 时区
	 */
	public static final String CONTEXT_TIME_ZONE = CONTEXT_PREFIX + "timeZone";
	/**
	 * 系统域名
	 */
	public static final String CONTEXT_SERVER_HOST = CONTEXT_PREFIX + "serverHost";
	/**
	 * 登录平台
	 */
	public static final String CONTEXT_PLATFORM = CONTEXT_PREFIX + "platform";
	/**
	 * 设备型号
	 */
	public static final String CONTEXT_DEVICE = CONTEXT_PREFIX + "device";
	/**
	 * context map 最大容量
	 */
	public static final Integer MAX_CAPACITY = 100;
	/**
	 * context map key 或者 value 最大值
	 */
	public static final Integer MAX_SIZE = 1024;

	private final Map<String, String> contextMap;

	/**
	 * 构造函数
	 */
	public SystemContext() {
		super();
		this.contextMap = new HashMap<>();
	}

	public Map<String, String> getContextMap() {
		return this.contextMap;
	}

	/**
	 * （获取键下的值.如果不存在，返回null；如果名值Map未初始化，也返回null） Get the value of key. Would
	 * return null if context map hasn't been initialized.
	 *
	 * @param key 键
	 * @return 键下的值
	 */
	public String get(String key) {
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
	public void set(String key, String value) {
		if (key == null || value == null) {
			log.error("key:" + key + " or value:" + value + " is null,i can't set it into the context map");
			return;
		}
		if (key.length() > MAX_SIZE) {
			throw new RuntimeException("key is more than " + MAX_SIZE + ", i can't set it into the context map");
		}
		if (value.length() > MAX_SIZE) {
			throw new RuntimeException("value is more than " + MAX_SIZE + ", i can't set it into the context map");
		}
		Map<String, String> contextMap = getContextMap();
		if (contextMap.size() > MAX_CAPACITY) {
			throw new RuntimeException("the context map is full, can't set anything");
		}
		contextMap.put(key, value);
	}

	public String getUserToken() {
		return get(CONTEXT_USER_TOKEN);
	}


	public void setUserToken(String token) {
		set(CONTEXT_USER_TOKEN, token);
	}


	public String getUserId() {
		return get(CONTEXT_USER_ID);
	}


	public void setUserId(String userId) {
		set(CONTEXT_USER_ID, userId);
	}


	public String getUserName() {
		return get(CONTEXT_USER_NAME);
	}


	public void setUserName(String userName) {
		set(CONTEXT_USER_NAME, userName);
	}


	public String getUserAgent() {
		return get(CONTEXT_USER_AGENT);
	}


	public void setUserAgent(String userAgent) {
		set(CONTEXT_USER_AGENT, userAgent);
	}


	public String getAccountId() {
		return get(CONTEXT_ACCOUNT_ID);
	}


	public void setAccountId(String accountId) {
		set(CONTEXT_ACCOUNT_ID, accountId);
	}


	public String getAccountName() {
		return get(CONTEXT_ACCOUNT_NAME);
	}


	public void setAccountName(String accountName) {
		set(CONTEXT_ACCOUNT_NAME, accountName);
	}


	public String getAppId() {
		return get(CONTEXT_APP_ID);
	}


	public void setAppId(String appId) {
		set(CONTEXT_APP_ID, appId);
	}


	public String getAppVersion() {
		return get(CONTEXT_APP_VERSION);
	}


	public void setAppVersion(String appVersion) {
		set(CONTEXT_APP_VERSION, appVersion);
	}


	public Integer getTimeZone() {
		String timeZone = get(CONTEXT_TIME_ZONE);
		return timeZone == null ? null : Integer.parseInt(timeZone);
	}


	public void setTimeZone(Integer timeZone) {
		set(CONTEXT_TIME_ZONE, timeZone + "");
	}


	public String getServerHost() {
		return get(CONTEXT_SERVER_HOST);
	}


	public void setServerHost(String serverHost) {
		set(CONTEXT_SERVER_HOST, serverHost);
	}


	public String getPlatform() {
		return get(CONTEXT_PLATFORM);
	}


	public void setPlatform(String device) {
		set(CONTEXT_PLATFORM, device);
	}


	public String getDevice() {
		return get(CONTEXT_DEVICE);
	}


	public void setDevice(String device) {
		set(CONTEXT_DEVICE, device);
	}


	public String getTenantId() {
		String tenantCode = get(CONTEXT_TENANT_ID);
		if (tenantCode == null || tenantCode.isEmpty()) {
			return DEFAULT_TENANT_ID;
		}
		return tenantCode;
	}


	public void setTenantId(String tenantId) {
		set(CONTEXT_TENANT_ID, tenantId);
	}


	public String getLocale() {
		String locale = get(CONTEXT_LOCALE);
		if (locale == null || locale.isEmpty()) {
			return DEFAULT_LOCALE;
		}
		return locale;
	}


	public void setLocale(String locale) {
		set(CONTEXT_LOCALE, locale);
	}


	public String getClientIp() {
		return get(CONTEXT_CLIENT_IP);
	}


	public void setClientIp(String clientIp) {
		set(CONTEXT_CLIENT_IP, clientIp);
	}

	public List<Pair<String, String>> toHeaders() {
		if (contextMap.isEmpty()) {
			return Collections.emptyList();
		}
		List<Pair<String, String>> pairs = new ArrayList<>();
		for (Map.Entry<String, String> entry : contextMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			if (StringUtils.isBlank(value)) {
				log.warn("header:" + key + "'s value:" + value + " is empty,will not add to headers");
				continue;
			}
			if (!StringUtils.startsWithIgnoreCase(key, CONTEXT_PREFIX)) {
				continue;
			}
			if (log.isDebugEnabled()) {
				log.debug("adding header{" + key + ":" + value + "}");
			}
			if (StringUtils.equalsIgnoreCase(key, CONTEXT_ACCOUNT_NAME)
					|| StringUtils.equalsIgnoreCase(key, CONTEXT_USER_NAME)) {
				pairs.add(convertKey(key, value, true));
			} else {
				pairs.add(convertKey(key, value, false));
			}
		}
		return pairs;
	}

	/**
	 * 增加header,可被子类重写
	 *
	 * @param name     header name
	 * @param value    header value
	 * @param encoding need encoding
	 */
	private Pair<String, String> convertKey(String name, String value, boolean encoding) {
		if (encoding) {
			try {
				return Pair.of(name, URLEncoder.encode(value, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				if (log.isDebugEnabled()) {
					log.error("can't convert value:" + value + " to utf-8,will set the raw value");
				}
				return Pair.of(name, value);
			}
		} else {
			return Pair.of(name, value);
		}
	}


}
