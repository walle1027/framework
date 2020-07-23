package org.loed.framework.common.web.mvc.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.context.SystemContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/7 11:41 AM
 */
@Slf4j
public class SystemContextFilter implements Filter {
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		try {
			if (log.isDebugEnabled()) {
				log.debug("system context init");
			}
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String curHeader = headerNames.nextElement();
				if (StringUtils.startsWithIgnoreCase(curHeader, SystemContext.CONTEXT_PREFIX)) {
					String headerVal = request.getHeader(curHeader);
					String requestURI = request.getRequestURI();
					if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_ACCOUNT_ID)) {
						SystemContext.set(SystemContext.CONTEXT_ACCOUNT_ID, headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_ACCOUNT_ID, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_ACCOUNT_NAME)) {
						headerVal = URLDecoder.decode(headerVal, "utf-8");
						SystemContext.set(SystemContext.CONTEXT_ACCOUNT_NAME, headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_ACCOUNT_NAME, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_USER_ID)) {
						SystemContext.set(SystemContext.CONTEXT_USER_ID, headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_USER_ID, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_USER_NAME)) {
						headerVal = URLDecoder.decode(headerVal, "utf-8");
						SystemContext.set(SystemContext.CONTEXT_USER_NAME, headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_USER_NAME, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_CLIENT_IP)) {
						SystemContext.set(SystemContext.CONTEXT_CLIENT_IP, headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_CLIENT_IP, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_USER_AGENT)) {
						SystemContext.set(SystemContext.CONTEXT_USER_AGENT, headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_USER_AGENT, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_TENANT_CODE)) {
						SystemContext.set(SystemContext.CONTEXT_TENANT_CODE, headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_TENANT_CODE, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_LOCALE)) {
						SystemContext.set(SystemContext.CONTEXT_LOCALE, headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_LOCALE, headerVal);
						}
					} else {
						SystemContext.set(curHeader, headerVal);
					}
				}
			}
			chain.doFilter(servletRequest, servletResponse);
		} catch (Throwable ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			SystemContext.clean();
		}
	}
}
