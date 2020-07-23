package org.loed.framework.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestUtils {
	public static String getRealLocation(ServletContext servletContext, String location) {
		// Return a URL (e.g. "classpath:" or "file:") as-is;
		// consider a plain file path as relative to the web application root directory.
		try {
			// Return a URL (e.g. "classpath:" or "file:") as-is;
			// consider a plain file path as relative to the web application root directory.
			if (!ResourceUtils.isUrl(location)) {
				// Resolve system property placeholders before resolving real path.
				location = SystemPropertyUtils.resolvePlaceholders(location);
				location = WebUtils.getRealPath(servletContext, location);
			}

			// Write log message to server log.
			servletContext.log("get location from [" + location + "]");
			String resolvedLocation = SystemPropertyUtils.resolvePlaceholders(location);
			URL url = ResourceUtils.getURL(resolvedLocation);
			return url.getPath();
		} catch (FileNotFoundException ex) {
			throw new IllegalArgumentException("Invalid 'location' parameter: " + ex.getMessage());
		}
	}

	/**
	 * 是否为POST请求
	 *
	 * @param request 请求对象
	 * @return 是否为post请求
	 */
	public static boolean isPost(HttpServletRequest request) {
		String method = request.getMethod();
		return "POST".equalsIgnoreCase(method);
	}

	/**
	 * 是否为Ajax请求
	 *
	 * @param request 请求对象
	 * @return 是否为ajax请求
	 */
	public static boolean isAjax(HttpServletRequest request) {
		String header = request.getHeader("X-Requested-With");
		return header != null && "XMLHttpRequest".equalsIgnoreCase(header);
	}


	/**
	 * 构建查询条件
	 *
	 * @return
	 */
	public static Map<String, String> buildQueryParam(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		for (Object o : request.getParameterMap().keySet()) {
			String key = o.toString();
			String[] parameterValues = request.getParameterValues(key);
			if (parameterValues.length == 0) {
				map.put(key, parameterValues[0]);
			} else {
				StringBuilder builder = new StringBuilder();
				for (String parameterValue : parameterValues) {
					builder.append(",");
					builder.append(parameterValue);
				}
				builder.deleteCharAt(0);
				map.put(key, builder.toString());
			}
		}
		return map;
	}

	/**
	 * 获取客户端的ip地址
	 *
	 * @param request 请求对象
	 * @return ip地址
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static Map<String, String> analyzeObjectAttrs(HttpServletRequest request, Class targetClass, String targetAlias) {
		//由于java是静态语言，所以类信息是固定的，因此可以缓存反射的结果
		List<Field> fields = ReflectionUtils.getDeclaredFields(targetClass);
		Map<String, String> resultMap = new HashMap<>();
		Map parameterMap = request.getParameterMap();
		StringBuilder builder = new StringBuilder();
		for (Field field : fields) {
			Class<?> fieldType = field.getType();
			int dataType = DataType.getDataType(fieldType);
			String fieldName = field.getName();
			if (!contains(parameterMap, targetAlias + fieldName)) {
				continue;
			}
			if (DataType.isSimpleType(dataType)) {
				builder.append(",");
				builder.append(fieldName);
			} else if (DataType.isCollectionType(dataType)) {
				Class collectionType = ReflectionUtils.getGenericCollectionType(field);
				if (collectionType != null) {
					resultMap.putAll(analyzeObjectAttrs(request, collectionType, targetAlias + fieldName + "[0]."));
				}
			} else if (DataType.isMapType(dataType)) {
				//TODO
			} else {
				resultMap.putAll(analyzeObjectAttrs(request, fieldType, targetAlias + fieldName + "."));
			}
		}
		if (builder.length() > 0) {
			resultMap.put(targetClass.getName(), builder.deleteCharAt(0).toString());
		}
		return resultMap;
	}

	/**
	 * 判断包含
	 *
	 * @return 是否匹配
	 */
	private static boolean contains(Map map, String string) {
		boolean flag = false;
		for (Object object : map.keySet()) {
			String s = object.toString();
			if (s.startsWith("__checkbox_")) {
				s = s.substring("__checkbox_".length());
			}
			if (s.startsWith(string)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public static String getInputStreamAsString(HttpServletRequest request) throws IOException {
		String str = new String(IOUtils.toByteArray(request.getInputStream()), "utf-8");
		if (StringUtils.isEmpty(str)) {
			return null;
		}
		return URLDecoder.decode(str, "utf-8");
	}

	public static <X> X getInputStreamAsObject(HttpServletRequest request, TypeReference<X> typeReference) throws IOException {
		String inputStreamAsString = getInputStreamAsString(request);
		if (StringUtils.isEmpty(inputStreamAsString)) {
			return null;
		}
		return SerializeUtils.fromJson(inputStreamAsString, typeReference);
	}


	public static String getMobileOs(HttpServletRequest request) {
		String header = request.getHeader("user-agent");
		if (StringUtils.isEmpty(header)) {
			return null;
		}
		header = header.toUpperCase();
		if (header.contains("ANDROID")) {
			return "android";
		} else if (header.contains("IOS") || header.contains("IPHONE") || header.contains("IPAD") || header.contains("ITOUCH")) {
			return "ios";
		}
		return null;
	}

	public static boolean isMobile(HttpServletRequest request) {
		return getMobileOs(request) != null;
	}

	public static byte[] getRequestFile(HttpServletRequest request, String fileName) {
		DiskFileItemFactory fac = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(fac);
		try {
			List<FileItem> fileItems = upload.parseRequest(request);
			if (fileItems != null) {
				for (FileItem fileItem : fileItems) {
					if (fileItem.isFormField()) {
						continue;
					}
					String name = fileItem.getName();
					if (StringUtils.isBlank(name)) {
						continue;
					}
					if (StringUtils.isNotEmpty(fileName) && fileName.equals(fileItem.getFieldName())) {
						return fileItem.get();
					}
					return fileItem.get();
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Convenience method to get the application's URL based on request
	 * variables.
	 */
	public static String getAppURL(HttpServletRequest request) {
		StringBuilder url = new StringBuilder();
		int port = request.getServerPort();
		if (port < 0) {
			port = 80; // Work around java.net.URL bug
		}
		String scheme = request.getScheme();
		url.append(scheme);
		url.append("://");
		url.append(request.getServerName());
		if ((scheme.equals("http") && (port != 80))
				|| (scheme.equals("https") && (port != 443))) {
			url.append(':');
			url.append(port);
		}
		url.append(request.getContextPath());
		return url.toString();
	}
}
