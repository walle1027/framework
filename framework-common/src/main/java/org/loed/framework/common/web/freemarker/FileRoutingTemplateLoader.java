package org.loed.framework.common.web.freemarker;

import freemarker.cache.TemplateLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.loed.framework.common.CopyOnWriteMap;
import org.loed.framework.common.SystemConstant;
import org.loed.framework.common.context.SystemContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FileRoutingTemplateLoader implements TemplateLoader {
	private static final Logger logger = LoggerFactory.getLogger(FileRoutingTemplateLoader.class);

	private String LAST_TEMPLATE_DIR = "_last_template_dir";

	private String templateLoaderPath;
	private CopyOnWriteMap<String, File> routingMap = new CopyOnWriteMap<>();

	public FileRoutingTemplateLoader() {
	}

	/**
	 * @param templateLoaderPath templateLoaderPath
	 */
	public FileRoutingTemplateLoader(String templateLoaderPath) {
		setTemplateLoaderPath(templateLoaderPath);
	}

	/**
	 * 扫描文件路由信息
	 */
	public void scanRoutingFiles() {
		String templatePath = getTemplatePath(templateLoaderPath);
		File templateDir = new File(templatePath);
		if (!templateDir.exists()) {
			return;
		}
		Collection<File> files = FileUtils.listFiles(templateDir, null, true);
		Map<String, File> map = new HashMap<>();
		if (CollectionUtils.isNotEmpty(files)) {
			for (File file : files) {
				String relativePath = file.getAbsolutePath().substring(templateDir.getAbsolutePath().length());
				relativePath = relativePath.replace(File.separator, "/");
				if (relativePath.startsWith("/")) {
					relativePath = relativePath.substring(1);
				}
				map.put(relativePath, file);
			}
			routingMap.putAll(map);
		}
	}

	@Override
	public Object findTemplateSource(String name) throws IOException {
		String path = null;
		try {
			String appVersion = SystemContext.getAppVersion();
			path = SystemContext.getTenantCode() + "/" + appVersion + "/" + name;
			if (routingMap.containsKey(path)) {
				return routingMap.get(path);
			}
			/*String realPath = getTemplatePath(path);
			if (realPath != null) {
				File file = new File(realPath);
				if (file.isFile() && file.canRead()) {
					return file;
				}
			}*/
			logger.debug("template:" + name + " for tenant:[" + SystemContext.getTenantCode() + "] and version:[" + appVersion + "] don'st exists");
			path = SystemContext.getTenantCode() + "/" + SystemConstant.DEFAULT_VERSION + "/" + name;
			if (routingMap.containsKey(path)) {
				return routingMap.get(path);
			}
			/*realPath = getTemplatePath(path);
			if (realPath != null) {
				File file = new File(realPath);
				if (file.isFile() && file.canRead()) {
					return file;
				}
			}*/
			logger.debug("template:" + name + " for tenant:[" + SystemContext.getTenantCode() + "] and version:[basic] don'st exists");
			path = SystemConstant.DEFAULT_TENANT_CODE + "/" + appVersion + "/" + name;
			if (routingMap.containsKey(path)) {
				return routingMap.get(path);
			}
			/*realPath = getTemplatePath(path);
			if (realPath != null) {
				File file = new File(realPath);
				if (file.isFile() && file.canRead()) {
					return file;
				}
			}*/
			logger.debug("template:" + name + " for tenant:" + SystemContext.getTenantCode() + " don'st exists");
			path = SystemConstant.DEFAULT_TENANT_CODE + "/" + SystemConstant.DEFAULT_VERSION + "/" + name;
			if (routingMap.containsKey(path)) {
				return routingMap.get(path);
			}
			/*realPath = getTemplatePath(path);
			if (realPath != null) {
				File file = new File(realPath);
				if (file.isFile() && file.canRead()) {
					return file;
				}
			}*/
			//增加其他优先级
			logger.debug("template:" + name + " for tenant:" + SystemConstant.DEFAULT_TENANT_CODE + " don'st exists");
			path = name;
			if (routingMap.containsKey(path)) {
				return routingMap.get(path);
			}
			/*realPath = getTemplatePath(path);
			if (realPath != null) {
				File file = new File(realPath);
				if (file.isFile() && file.canRead()) {
					return file;
				}
			}*/
			/*path = null;
			String lastTemplateDir = SystemContext.get(LAST_TEMPLATE_DIR);
			if (StringUtils.isNotEmpty(lastTemplateDir)) {
				String realPath = getTemplatePath(lastTemplateDir + "/" + name);
				if (realPath != null) {
					File file = new File(realPath);
					if (file.isFile() && file.canRead()) {
						return file;
					}
				}
			}*/
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getLastModified(Object templateSource) {
		if (templateSource instanceof File) {
			return ((File) templateSource).lastModified();
		}
		return 0;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding)
			throws IOException {
		if (templateSource instanceof File) {
			return new InputStreamReader(
					new FileInputStream((File) templateSource),
					encoding);
		}
		return null;
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
	}

	public String getTemplateLoaderPath() {
		return templateLoaderPath;
	}

	public void setTemplateLoaderPath(String templateLoaderPath) {
		if (templateLoaderPath == null) {
			throw new IllegalArgumentException("templateLoaderPath is null");
		}

		templateLoaderPath = templateLoaderPath.replace('\\', '/');
		if (!templateLoaderPath.endsWith("/")) {
			templateLoaderPath += "/";
		}
		if (!templateLoaderPath.startsWith("/")) {
			templateLoaderPath = "/" + templateLoaderPath;
		}
		this.templateLoaderPath = templateLoaderPath;
	}

	protected String getTemplatePath(String templatePath) {
		return templatePath;
	}
}
