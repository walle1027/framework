package org.loed.framework.common.plugin.provider;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.loed.framework.common.plugin.Plugin;
import org.loed.framework.common.plugin.PluginProvider;
import org.loed.framework.common.plugin.impl.InternalPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/11/28 12:06
 */

public class ConfigurablePluginProvider implements PluginProvider, InitializingBean {
	protected String separator = ":";
	private Map<String, Map<String, Plugin>> pluginMap = new HashMap<>();
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Resource[] plugins;

	public void addPlugin(Plugin plugin) {
		String firstKey = plugin.getSignature();
		String secondKey = plugin.getTenantCode();
		Map<String, Plugin> map = pluginMap.get(firstKey);
		if (map == null) {
			map = new HashMap<>();
		}
		map.put(secondKey, plugin);
		pluginMap.put(firstKey, map);
	}

	@Override
	public boolean hasPlugin(String tenantCode, String signature) {
		String key = tenantCode + separator + signature;
		return pluginMap.containsKey(key);
	}

	@Override
	public Plugin getPlugin(String tenantCode, String signature) {
		if (pluginMap.containsKey(signature)) {
			Map<String, Plugin> map = pluginMap.get(signature);
			if (map.containsKey(tenantCode)) {
				return map.get(tenantCode);
			}
			if (map.containsKey(ALL_SIGN)) {
				return map.get(ALL_SIGN);
			}
		}
		return null;
	}

	public void setPlugins(Resource[] plugins) {
		this.plugins = plugins;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (plugins != null && plugins.length > 0) {
			for (Resource pluginConfig : plugins) {
				if (logger.isDebugEnabled()) {
					logger.debug("开始加载插件配置文件：" + pluginConfig.getFilename());
				}
				loadPluginConfig(pluginConfig);
			}
		}
	}

	private void loadPluginConfig(Resource config) {
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(config.getInputStream());
			Element root = document.getRootElement();
			List elements = root.elements("internalPlugin");
			if (elements != null) {
				for (Object element : elements) {
					Element internal = (Element) element;
					InternalPlugin plugin = new InternalPlugin();
					String signature = internal.elementTextTrim("signature");
					if (StringUtils.isBlank(signature)) {
						throw new RuntimeException(config.getFilename() + " plugin's signature is empty");
					}
					plugin.setSignature(signature);

					String isSpringBean = internal.elementTextTrim("isSpringBean");
					if (StringUtils.isNotBlank(isSpringBean)) {
						plugin.setSpringBean(Boolean.valueOf(isSpringBean));
					}
					String beanName = internal.elementTextTrim("beanName");
					plugin.setBeanName(beanName);
					plugin.setBeanClass(internal.elementTextTrim("beanClass"));
					plugin.setMethod(internal.elementTextTrim("method"));
					String tenantCode = internal.elementTextTrim("tenantCode");
					if (StringUtils.isBlank(tenantCode)) {
						tenantCode = ALL_SIGN;
					}
					plugin.setTenantCode(tenantCode);
					addPlugin(plugin);
				}
			}
		} catch (DocumentException | IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
