package org.loed.framework.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-8-15 下午5:44
 */

public class ResponseUtils {
	// -- header 常量定义 --//
	private static final String ENCODING_PREFIX = "encoding";
	private static final String NOCACHE_PREFIX = "no-cache";
	private static final String ENCODING_DEFAULT = "UTF-8";
	private static final boolean NOCACHE_DEFAULT = true;

	// -- content-type 常量定义 --//
	private static final String TEXT_TYPE = "text/plain";
	private static final String JSON_TYPE = "application/json";
	private static final String XML_TYPE = "text/xml";
	private static final String HTML_TYPE = "text/html";
	private static final String JS_TYPE = "text/javascript";

	private static Log logger = LogFactory.getLog(ResponseUtils.class);

	// -- 绕过jsp/freemaker直接输出文本的函数 --//

	/**
	 * 直接输出内容的简便函数.
	 * <p/>
	 * eg. render("text/plain", "hello", "encoding:GBK"); render("text/plain",
	 * "hello", "no-cache:false"); render("text/plain", "hello", "encoding:GBK",
	 * "no-cache:false");
	 *
	 * @param headers 可变的header数组，目前接受的值为"encoding:"或"no-cache:",默认值分别为UTF-8和true.
	 */
	public static void render(final HttpServletResponse response, final String contentType, final String content,
	                          final String... headers) {
		try {
			// 分析headers参数
			String encoding = ENCODING_DEFAULT;
			boolean noCache = NOCACHE_DEFAULT;
			for (String header : headers) {
				String headerName = StringUtils.substringBefore(header, ":");
				String headerValue = StringUtils.substringAfter(header, ":");

				if (StringUtils.equalsIgnoreCase(headerName, ENCODING_PREFIX)) {
					encoding = headerValue;
				} else if (StringUtils.equalsIgnoreCase(headerName,
						NOCACHE_PREFIX)) {
					noCache = Boolean.parseBoolean(headerValue);
				} else {
					throw new IllegalArgumentException(headerName
							+ "不是一个合法的header类型");
				}
			}

			// 设置headers参数
			String fullContentType = contentType + ";charset=" + encoding;
			response.setContentType(fullContentType);
			if (noCache) {
				response.setHeader("Pragma", "No-cache");
				response.setHeader("Cache-Control", "no-cache");
				response.setDateHeader("Expires", 0);
			}
			response.getWriter().write(content);
			response.getWriter().flush();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 直接输出文本.
	 *
	 * @see #render(HttpServletResponse, String, String, String...)
	 */
	public static void renderText(final HttpServletResponse response, final String text, final String... headers) {
		render(response, TEXT_TYPE, text, headers);
	}

	/**
	 * 直接输出HTML.
	 *
	 * @see #render(HttpServletResponse, String, String, String...)
	 */
	public static void renderHtml(final HttpServletResponse response, final String html, final String... headers) {
		render(response, HTML_TYPE, html, headers);
	}

	/**
	 * 直接输出XML.
	 *
	 * @see #render(HttpServletResponse, String, String, String...)
	 */
	public static void renderXml(final HttpServletResponse response, final String xml, final String... headers) {
		render(response, XML_TYPE, xml, headers);
	}

	/**
	 * 直接输出JSON.
	 *
	 * @param jsonString json字符串.
	 * @see #render(HttpServletResponse, String, String, String...)
	 */
	public static void renderJson(final HttpServletResponse response, final String jsonString,
	                              final String... headers) {
		render(response, JSON_TYPE, jsonString, headers);
	}

	/**
	 * 直接输出JSON.
	 *
	 * @param object Java对象,将被转化为json字符串.
	 * @see #render(HttpServletResponse, String, String, String...)
	 */
	public static void renderJson(final HttpServletResponse response, final Object object) {
		renderJson(response, object, false);
	}

	/**
	 * 直接输出JSON.
	 *
	 * @param object Java对象,将被转化为json字符串.
	 * @see #render(HttpServletResponse, String, String, String...)
	 */
	public static void renderJson(final HttpServletResponse response, final Object object, boolean ignoreNull) {
		String jsonString = SerializeUtils.toJson(object);
		render(response, JSON_TYPE, jsonString);
	}

	/**
	 * 直接输出JSON.
	 *
	 * @see #render(HttpServletResponse, String, String, String...)
	 */
	public static void renderJsonStr(final HttpServletResponse response, final String jsonString) {
		render(response, JSON_TYPE, jsonString);
	}

	/**
	 * 直接输出支持跨域Mashup的JSONP.
	 *
	 * @param callbackName callback函数名.
	 * @param contentMap   Map对象,将被转化为json字符串.
	 * @see #render(HttpServletResponse, String, String, String...)
	 */
	@SuppressWarnings("unchecked")
	public static void renderJsonp(final HttpServletResponse response, final String callbackName,
	                               final Map contentMap, final String... headers) {
		String jsonParam = SerializeUtils.toJson(contentMap);
		StringBuilder result = new StringBuilder().append(callbackName).append(
				"(").append(jsonParam).append(");");

		// 渲染Content-Type为javascript的返回内容,输出结果为javascript语句,
		// 如callback197("{content:'Hello World!!!'}");
		render(response, JS_TYPE, result.toString(), headers);
	}

	public static void writeErrorResult(final HttpServletResponse response, int errorCode, String errorMessage) {
		response.setStatus(errorCode);
		Map<String, String> map = new HashMap<>();
		map.put("code", errorCode + "");
		map.put("message", errorMessage);
		renderText(response, SerializeUtils.toJson(map));
	}

	public static void setDownloadFileName(HttpServletRequest request, HttpServletResponse resp, String fileName) throws UnsupportedEncodingException {
		String new_filename = URLEncoder.encode(fileName, "UTF-8");
		String rtn = "filename=\"" + new_filename + "\"";
		String userAgent = request.getHeader("user-agent");
		if (userAgent != null) {
			userAgent = userAgent.toLowerCase();
			// IE浏览器，只能采用URLEncoder编码
			if (userAgent.contains("msie")) {
				rtn = "filename=" + new_filename + "";
			}
			// Opera浏览器只能采用filename*
			else if (userAgent.contains("opera")) {
				rtn = "filename*=UTF-8''" + new_filename;
			}
			// Safari浏览器，只能采用ISO编码的中文输出
			else if (userAgent.contains("safari")) {
				rtn = "filename=" + new String(fileName.getBytes("UTF-8"), "ISO8859-1") + "";
			}
			// Chrome浏览器，只能采用MimeUtility编码或ISO编码的中文输出
			else if (userAgent.contains("applewebkit")) {
				rtn = "filename=" + new_filename + "";
			}
			// FireFox浏览器，可以使用MimeUtility或filename*或ISO编码的中文输出
			else if (userAgent.contains("mozilla")) {
				rtn = "filename*=UTF-8''" + new_filename;
			}
		}
		resp.setHeader("Content-Disposition", "'attachment';" + rtn);
	}
}
