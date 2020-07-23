package org.loed.framework.common.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Convenience class for setting and retrieving cookies.
 */
public class CookieUtils {

	/**
	 * Convenience method to set a cookie
	 *
	 * @param response
	 * @param name
	 * @param value
	 * @param path
	 */
	public static void setCookie(HttpServletResponse response, String name,
	                             String value, String path, String age) {
		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(false);
		cookie.setPath(path);
		if (age != null) {
			int n = 2;
			try {
				n = Integer.parseInt(age);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			cookie.setMaxAge(3600 * n); // 2 hours
		}
		response.addCookie(cookie);
	}

	public static void setCookie(HttpServletResponse response, String name,
	                             String value, String path) {
		setCookie(response, name, value, path, null);
	}

	public static void setCookie(HttpServletResponse response, String name,
	                             String value) {
		setCookie(response, name, value, "/", null);
	}

	public static void setCookie(HttpServletResponse response, String name,
	                             String value, String path, String age, String domain) {
		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(false);
		cookie.setPath(path);
		cookie.setDomain(domain);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		if (age != null) {
			int n = 2;
			try {
				n = Integer.parseInt(age);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			cookie.setMaxAge(3600 * n); // 2 hours
		}
		response.addCookie(cookie);
	}

	public static String getCookie(HttpServletRequest request, String name, String path, String domain) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				String cookieDomain = cookie.getDomain();
				String cookiePath = cookie.getPath();
				if (name.equals(cookieName) && path.equals(cookiePath) && domain.equals(cookieDomain)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Convenience method to get a cookie by name
	 *
	 * @param request the current request
	 * @param name    the name of the cookie to find
	 * @return the cookie (if found), null if not found
	 */
	public static Cookie getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie thisCookie : cookies) {
			if (thisCookie.getName().equals(name)) {
				return thisCookie;
			}
		}
		return null;
	}

	/**
	 * Convenience method to get a cookie by name
	 *
	 * @param request the current request
	 * @param name    the name of the cookie to find
	 * @return the cookie (if found), null if not found
	 */
	public static Cookie getCookie(HttpServletRequest request, String domain, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name) && cookie.getDomain().equals(domain)) {
				return cookie;
			}
		}
		return null;
	}

	public static String getCookieValue(HttpServletRequest request, String name) {
		Cookie cookie = getCookie(request, name);
		if (cookie == null) {
			return null;
		}
		return cookie.getValue();
	}

	public static String getCookieValue(HttpServletRequest request, String domain, String name) {
		Cookie cookie = getCookie(request, domain, name);
		if (cookie == null) {
			return null;
		}
		return cookie.getValue();
	}

	public static String getAllCookieNameAndValue(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		StringBuilder cs = new StringBuilder();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				cs.append(cookie.getName()).append("=")
						.append(cookie.getValue()).append(";");
			}
		}
		return cs.toString();
	}

	/**
	 * Convenience method for deleting a cookie by name
	 *
	 * @param response the current web response
	 * @param cookie   the cookie to delete
	 * @param path     the path on which the cookie was set
	 */
	public static void deleteCookie(HttpServletResponse response,
	                                Cookie cookie, String path) {
		if (cookie != null) {
			// Delete the cookie by setting its maximum age to zero
			cookie.setMaxAge(0);
			cookie.setPath(path);
			response.addCookie(cookie);
		}
	}

	public static void deleteCookie(HttpServletResponse response,
	                                Cookie cookie) {
		deleteCookie(response, cookie, "/");
	}
}
