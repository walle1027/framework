package org.loed.framework.common.web.mvc.listener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;

/**
 * 对hibernate中实体的关系进行一个遍历扫描的工具
 *
 * @author Thomason
 * @version 1.0
 * @since 11-10-10 下午9:07
 */

public class EntityScanner implements ServletContextListener {
	private static final String RESOURCE_PATTERN = "/**/*.class";
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	/**
	 * * Notification that the web application initialization
	 * * process is starting.
	 * * All ServletContextListeners are notified of context
	 * * initialization before any filter or servlet in the web
	 * * application is initialized.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String packagesToScan = sce.getServletContext().getInitParameter("packagesToScan");
		if (StringUtils.isBlank(packagesToScan)) {
			throw new RuntimeException("初始化参数:initalPath 不能为空！");
		}
		String[] packages = packagesToScan.split(".");
		scanEntities(packages);
	}

	/**
	 * @param packages
	 */
	protected void scanEntities(String[] packages) {
		for (String pkg : packages) {
			scanEntities(pkg);
		}
	}

	/**
	 * @param pkg
	 */
	protected void scanEntities(String pkg) {
		try {
			String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					ClassUtils.convertClassNameToResourcePath(pkg) + RESOURCE_PATTERN;
			Resource[] resources = this.resourcePatternResolver.getResources(pattern);
			for (Resource resource : resources) {
				if (resource.isReadable()) {
					Class clazz = resource.getClass();
					scanClass(clazz);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to scan classpath for unlisted classes", e);
		}

	}

	/**
	 * 扫描一个实体类，将它的属性，以及它的关联属性都以路径的方式扫描出来，并且将每个属性，及属性的
	 *
	 * @param clazz
	 */
	protected void scanClass(Class clazz) {
	}

	/**
	 * * Notification that the servlet context is about to be shut down.
	 * * All servlets and filters have been destroy()ed before any
	 * * ServletContextListeners are notified of context
	 * * destruction.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}
}
