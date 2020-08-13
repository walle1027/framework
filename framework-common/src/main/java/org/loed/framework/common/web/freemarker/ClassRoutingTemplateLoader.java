package org.loed.framework.common.web.freemarker;

import freemarker.cache.URLTemplateLoader;
import org.loed.framework.common.SystemConstant;
import org.loed.framework.common.context.SystemContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/9/30 15:57
 */

public class ClassRoutingTemplateLoader extends URLTemplateLoader {
	private static final Logger logger = LoggerFactory.getLogger(FileRoutingTemplateLoader.class);

	private String templateLoaderPath = "";

	@Override
	protected URL getURL(String name) {
		String fullPath = null;
		String appVersion = SystemContextHolder.getAppVersion();
		fullPath = templateLoaderPath + "/" + SystemContextHolder.getTenantCode() + "/" + appVersion + "/" + name;
		URL url = Thread.currentThread().getContextClassLoader().getResource(fullPath);
		if (url != null) {
			return url;
		}
		logger.debug("template:" + name + " for tenant:[" + SystemContextHolder.getTenantCode() + "] and version:[" + appVersion + "] don'st exists");
		fullPath = templateLoaderPath + "/" + SystemContextHolder.getTenantCode() + "/" + SystemConstant.DEFAULT_VERSION + "/" + name;
		url = Thread.currentThread().getContextClassLoader().getResource(fullPath);
		if (url != null) {
			return url;
		}
		logger.debug("template:" + name + " for tenant:[" + SystemContextHolder.getTenantCode() + "] and version:[basic] don'st exists");
		fullPath = templateLoaderPath + "/" + SystemConstant.DEFAULT_TENANT_CODE + "/" + appVersion + "/" + name;
		url = Thread.currentThread().getContextClassLoader().getResource(fullPath);
		if (url != null) {
			return url;
		}
		logger.debug("template:" + name + " for tenant:" + SystemContextHolder.getTenantCode() + " don'st exists");
		fullPath = templateLoaderPath + "/" + SystemConstant.DEFAULT_TENANT_CODE + "/" + SystemConstant.DEFAULT_VERSION + "/" + name;
		url = Thread.currentThread().getContextClassLoader().getResource(fullPath);
		if (url != null) {
			return url;
		}
		logger.debug("template:" + name + " for tenant:" + SystemConstant.DEFAULT_TENANT_CODE + " don'st exists");
		fullPath = templateLoaderPath + "/" + name;
		url = Thread.currentThread().getContextClassLoader().getResource(fullPath);
		if (url != null) {
			return url;
		}
		return null;
	}

	public String getTemplateLoaderPath() {
		return templateLoaderPath;
	}

	public void setTemplateLoaderPath(String templateLoaderPath) {
		this.templateLoaderPath = templateLoaderPath;
	}
}
