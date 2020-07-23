package org.loed.framework.common.web.mvc;

import com.fasterxml.jackson.core.type.TypeReference;
import org.loed.framework.common.util.RequestUtils;
import org.loed.framework.common.util.ResponseUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015-12-1 16:50
 */

public class SpringWebUtils {
	/**
	 * 用于保存线程相关信息
	 */
	transient static ThreadLocal<Map<String, Object>> threadLocalMap = new ThreadLocal<Map<String, Object>>();

	/**
	 * 构造函数
	 */
	public SpringWebUtils() {
		// For Spring initialization.
	}

	/**
	 * 从 ThreadLocal中获取名值Map(不包含appCode)
	 *
	 * @return 名值Map
	 */
	public static Map<String, Object> getContextMap() {
		return threadLocalMap.get();
	}

	/**
	 * 从 ThreadLocal 获取名值Map
	 *
	 * @param contextMap 名值Map
	 */
	public static void setContextMap(Map<String, Object> contextMap) {
		SpringWebUtils.threadLocalMap.set(contextMap);
	}

	/**
	 * （获取键下的值.如果不存在，返回null；如果名值Map未初始化，也返回null） Get the value of key. Would
	 * return null if context map hasn't been initialized.
	 *
	 * @param key 键
	 * @return 键下的值
	 */
	private static Object get(String key) {
		Map<String, Object> contextMap = getContextMap();
		if (contextMap == null) {
			return null;
		}
		return contextMap.get(key);
	}

	public static void clean() {
		threadLocalMap.remove();
	}

	/**
	 * （设置名值对。如果Map之前为null，则会被初始化） Put the key-value into the context map;
	 * <p/>
	 * Initialize the map if the it doesn't exist.
	 *
	 * @param key   键
	 * @param value 值
	 * @return 之前的值
	 */
	private static Object put(String key, Object value) {
		Map<String, Object> contextMap = getContextMap();
		if (contextMap == null) {
			contextMap = new HashMap<String, Object>();
			setContextMap(contextMap);
		}
		return contextMap.put(key, value);
	}

	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) get("request");
	}

	public static void setRequest(HttpServletRequest request) {
		put("request", request);
	}

	public static HttpServletResponse getResponse() {
		return (HttpServletResponse) get("response");
	}

	public static void setResponse(HttpServletResponse response) {
		put("response", response);
	}

	public static HttpSession getSession() {
		return (HttpSession) get("session");
	}

	public static void setSession(HttpSession session) {
		put("session", session);
	}

	/**
	 * 返回json数据
	 *
	 * @param object json对象
	 */
	public static void renderJson(Object object) {
		ResponseUtils.renderJson(getResponse(), object, true);
	}


	public static String getInputStreamAsString() throws IOException {
		HttpServletRequest request = getRequest();
		return RequestUtils.getInputStreamAsString(request);
	}

	public static <X> X getInputStreamAsObject(TypeReference<X> typeReference) throws IOException {
		HttpServletRequest request = getRequest();
		return RequestUtils.getInputStreamAsObject(request, typeReference);
	}

	public static byte[] getRequestFile(String fileName) {
		return getRequestFile(getRequest(), fileName);
	}


	public static byte[] getRequestFile(HttpServletRequest request, String fileName) {
		try {
			//解析器解析request的上下文
			CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
			//先判断request中是否包涵multipart类型的数据，
			if (!multipartResolver.isMultipart(request)) {
				throw new RuntimeException("not a multipartForm");
			}
			//再将request中的数据转化成multipart类型的数据
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Iterator iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				MultipartFile file = multiRequest.getFile((String) iter.next());
				if (file == null) {
					continue;
				}
				String originalFilename = file.getOriginalFilename();
				if (StringUtils.isEmpty(originalFilename)) {
					continue;
				}
				String name = file.getName();
				if (StringUtils.isEmpty(fileName)) {
					return file.getBytes();
				} else {
					if (fileName.equals(name)) {
						return file.getBytes();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
