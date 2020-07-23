package org.loed.framework.common.web.mvc.listener;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @author Thomason
 * @version 1.0
 * @since 12-4-1 下午11:29
 */
@WebListener
public class SysCfgInitialListener implements ServletContextListener {
	/**
	 * * Notification that the web application initialization
	 * * process is starting.
	 * * All ServletContextListeners are notified of context
	 * * initialization before any filter or servlet in the web
	 * * application is initialized.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String location = sce.getServletContext().getInitParameter("systemConfigLocation");
		ServletContext servletContext = sce.getServletContext();
		if (StringUtils.isNotEmpty(location)) {
			String[] strings = location.split(",");
			if (strings.length > 0) {
				for (String string : strings) {
					addConfigFiles(string, servletContext);
				}
			}
		}
		//增加配置项
		String contextName = servletContext.getContextPath();
//		SystemConfig.setConfig(SystemConstant.CONTEXT_NAME, contextName);
	}

	private void addConfigFiles(String location, ServletContext servletContext) {
//		SystemConfig.addConfigFiles(RequestUtils.getRealLocation(servletContext, location));
	}

	/**
	 * * Notification that the servlet context is about to be shut down.
	 * * All servlets and filters have been destroy()ed before any
	 * * ServletContextListeners are notified of context
	 * * destruction.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		//TODO
	}
}
