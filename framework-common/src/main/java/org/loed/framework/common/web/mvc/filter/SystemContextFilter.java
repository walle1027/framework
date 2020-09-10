package org.loed.framework.common.web.mvc.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.context.SystemContextHolder;
import org.springframework.core.Ordered;

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
public class SystemContextFilter implements Filter, Ordered {
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
						SystemContextHolder.setAccountId(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_ACCOUNT_ID, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_ACCOUNT_NAME)) {
						headerVal = URLDecoder.decode(headerVal, "utf-8");
						SystemContextHolder.setAccountName(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_ACCOUNT_NAME, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_USER_ID)) {
						SystemContextHolder.setUserId(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_USER_ID, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_USER_NAME)) {
						headerVal = URLDecoder.decode(headerVal, "utf-8");
						SystemContextHolder.setUserName(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_USER_NAME, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_CLIENT_IP)) {
						SystemContextHolder.setClientIp(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_CLIENT_IP, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_USER_AGENT)) {
						SystemContextHolder.setUserAgent(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_USER_AGENT, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_TENANT_ID)) {
						SystemContextHolder.setTenantCode(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_TENANT_ID, headerVal);
						}
					} else if (StringUtils.equalsIgnoreCase(curHeader, SystemContext.CONTEXT_LOCALE)) {
						SystemContextHolder.setLocale(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("request {} has  header: {}, with value {}", requestURI, SystemContext.CONTEXT_LOCALE, headerVal);
						}
					} else {
						SystemContextHolder.set(curHeader, headerVal);
					}
				}
			}
			chain.doFilter(servletRequest, servletResponse);
		} catch (Throwable ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			SystemContextHolder.clean();
		}
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
