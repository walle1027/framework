package org.loed.framework.common.mapping;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.loed.framework.common.mapping.config.MappingConfig;
import org.loed.framework.common.mapping.config.PropertyConfig;
import org.loed.framework.common.mapping.impl.MapperImpl;
import org.loed.framework.common.util.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Thomason
 * Date: 11-4-20
 * Time: 下午9:41
 * @version 1.0
 */
@SuppressWarnings({"unchecked"})
public class MapperFactoryBean implements FactoryBean<Mapper>, InitializingBean {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Resource[] configFiles;
	private MapperImpl mapper;

	public Mapper getObject() throws Exception {
		return mapper;
	}

	public Class<Mapper> getObjectType() {
		return Mapper.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		mapper = new MapperImpl();
		Map<String, MappingConfig> configMap = new HashMap<String, MappingConfig>();
		if (configFiles != null && configFiles.length > 0) {
			for (Resource configFile : configFiles) {
				if (logger.isDebugEnabled()) {
					logger.debug("开始配置映射文件：" + configFile.getFilename());
				}
				createConfig(configFile, configMap);
			}
		}
		mapper.setConfigMap(configMap);
		if (logger.isDebugEnabled()) {
			logger.debug("映射文件全部加载完成");
		}
	}

	/**
	 * 配置文件读取配置信息
	 *
	 * @param configFile 配置文件名称
	 * @param configMap  配置文件
	 * @throws Exception 异常
	 */
	private void createConfig(Resource configFile, Map<String, MappingConfig> configMap) throws Exception {
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(configFile.getInputStream());
			//解析xml文件，将xml中的配置映射到配置类中
			Element root = document.getRootElement();
			Iterator<Element> it = root.elementIterator("mapping");
			while (it.hasNext()) {
				Element mapping = it.next();
				//1 将xml配置映射到配置类中
				final MappingConfig config = xmlToConfig(mapping);
				//2 设置有效的复制列表
				config.calculateActualPropertyMappingList();
				configMap.put(config.getMappingKey(), config);

				//3 如果是双向映射的，那么配置双向的文件
				//如果是双向的关联并且源类和目标类不一致，将src和dest对调
				if (config.getDirection().equals("two-way") &&
						!config.getSrcClassType().equals(config.getDestClassType())) {
					MappingConfig opposite = copyTwoWayConfig(config);
					opposite.calculateActualPropertyMappingList();
					configMap.put(opposite.getMappingKey(), opposite);
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从一个配置类，生成它的反向配置类
	 *
	 * @param config 配置类
	 * @return 配置项
	 */
	protected MappingConfig copyTwoWayConfig(MappingConfig config) {
		MappingConfig opposite = new MappingConfig();
		opposite.setSrcClassType(config.getDestClassType());
		opposite.setDestClassType(config.getSrcClassType());
		opposite.setDirection(config.getDirection());
		opposite.setMappingId(config.getMappingId());
		opposite.setMappingKey(MappingConfig.buildMappingKey(opposite.getSrcClassType(), opposite.getDestClassType(), opposite.getMappingId()));
		//复制include列表
		if (config.getIncludeList() != null && config.getIncludeList().size() > 0) {
			List<PropertyConfig> includeList = new ArrayList<PropertyConfig>();
			for (PropertyConfig propertyConfig : config.getIncludeList()) {
				includeList.add(createPropertyConfig(propertyConfig));
			}
			opposite.setIncludeList(includeList);
		}
		//复制exclude列表
		if (config.getExcludeList() != null && config.getExcludeList().size() > 0) {
			List<PropertyConfig> excludeList = new ArrayList<PropertyConfig>();
			for (PropertyConfig propertyConfig : config.getExcludeList()) {
				excludeList.add(createPropertyConfig(propertyConfig));
			}
			opposite.setExcludeList(excludeList);
		}
		return opposite;

	}

	private PropertyConfig createPropertyConfig(PropertyConfig propertyConfig) {
		PropertyConfig include = new PropertyConfig();
		//对调源和目标
		include.setSrcPropertyName(propertyConfig.getDestPropertyName());
		include.setSrcPropertyType(propertyConfig.getDestPropertyType());
		include.setDestPropertyName(propertyConfig.getSrcPropertyName());
		include.setDestPropertyType(propertyConfig.getSrcPropertyType());
		return include;
	}

	/**
	 * 将xml的mapping节点的属性配置到config类中
	 *
	 * @param mapping xml中的mapping节点
	 * @return config类
	 * @throws ClassNotFoundException 类未定义异常
	 */
	protected MappingConfig xmlToConfig(Element mapping) throws ClassNotFoundException {
		MappingConfig config = new MappingConfig();
		//映射源class
		String srcClass = mapping.attributeValue("srcClass");
		if (StringUtils.isEmpty(srcClass)) {
			srcClass = mapping.elementText("srcClass");
		}
		Assert.hasText(srcClass, "srcClass 不能为空！");
		config.setSrcClassType(srcClass);
		Class<?> srcClazz = Class.forName(srcClass);

		//映射目标class
		String destClass = mapping.attributeValue("destClass");
		if (StringUtils.isEmpty(destClass)) {
			destClass = mapping.elementText("destClass");
		}
		//如果映射目标文件未配置，默认取源文件为目标文件
		if (StringUtils.isEmpty(destClass)) {
			destClass = config.getSrcClassType();
		}
		config.setDestClassType(destClass);
		Class<?> destClazz = Class.forName(destClass);

		//映射方向
		String direction = mapping.attributeValue("direction");
		if (StringUtils.isEmpty(direction)) {
			direction = mapping.elementText("direction");
		}
		if (StringUtils.isEmpty(direction)) {
			direction = "two-way";
		}
		config.setDirection(direction);

		//映射文件ID
		String mappingId = mapping.attributeValue("id");
		if (StringUtils.isEmpty(mappingId)) {
			mappingId = mapping.elementText("id");
		}
		config.setMappingId(mappingId);
		//设置映射Id
		config.setMappingKey(MappingConfig.buildMappingKey(srcClass, destClass, mappingId));

		Element includes = mapping.element("includes");
		if (includes != null) {
			List<Element> includesList = includes.elements("include");
			if (includesList != null && includesList.size() > 0) {
				List<PropertyConfig> includeList = new ArrayList<PropertyConfig>();
				for (Element element : includesList) {
					PropertyConfig propertyConfig = new PropertyConfig();
					String srcProperty = element.elementTextTrim("srcProperty");
					Assert.hasText(srcProperty, "class :[" + srcClass + "] has an empty field");
					propertyConfig.setSrcPropertyName(srcProperty);
					//适应map类型
					if (org.loed.framework.common.util.ReflectionUtils.isInterfaceOf(srcClazz, Map.class)) {
						propertyConfig.setSrcPropertyType(DataType.DT_Unknown);
					} else {
						Field srcPropertyType = ReflectionUtils.findField(srcClazz, srcProperty);
						Assert.notNull(srcPropertyType, "class :[" + srcClass + "] property:[" + srcProperty + "] not exists!");
						propertyConfig.setSrcPropertyType(DataType.getDataType(srcPropertyType.getType()));
					}
					String destProperty = element.elementTextTrim("destProperty");
					if (StringUtils.isEmpty(destProperty)) {
						destProperty = srcProperty;
					}
					propertyConfig.setDestPropertyName(destProperty);
					if (org.loed.framework.common.util.ReflectionUtils.isInterfaceOf(destClazz, Map.class)) {
						propertyConfig.setDestPropertyType(DataType.DT_Unknown);
					} else {
						Field destPropertyType = ReflectionUtils.findField(destClazz, destProperty);
						Assert.notNull(destPropertyType, "class :[" + destClass + "] property:[" + destProperty + "] not exists");
						propertyConfig.setDestPropertyType(DataType.getDataType(destPropertyType.getType()));
					}
					includeList.add(propertyConfig);
				}
				config.setIncludeList(includeList);
			}
		}

		Element excludes = mapping.element("excludes");
		if (excludes != null) {
			List<Element> excludesList = excludes.elements("exclude");
			if (excludesList != null && excludesList.size() > 0) {
				List<PropertyConfig> excludeList = new ArrayList<PropertyConfig>();
				for (Element element : excludesList) {
					PropertyConfig propertyConfig = new PropertyConfig();
					String srcProperty = element.elementTextTrim("srcProperty");
					Assert.hasText(srcProperty, "class :[" + srcClass + "] has an empty field");
					propertyConfig.setSrcPropertyName(srcProperty);
					Field srcPropertyType = ReflectionUtils.findField(srcClazz, srcProperty);
					Assert.notNull(srcPropertyType, "class :[" + srcClazz + "] property:[" + srcProperty + "] not exists!");
					propertyConfig.setSrcPropertyType(DataType.getDataType(srcPropertyType.getType()));

					String destProperty = element.elementTextTrim("destProperty");
					if (StringUtils.isEmpty(destProperty)) {
						destProperty = srcProperty;
					}
					propertyConfig.setDestPropertyName(destProperty);
					Field destPropertyType = ReflectionUtils.findField(destClazz, destProperty);
					Assert.notNull(destPropertyType, "class :[" + destClass + "] property:[" + destProperty + "] not exists");
					propertyConfig.setDestPropertyType(DataType.getDataType(destPropertyType.getType()));
					excludeList.add(propertyConfig);
				}
				config.setExcludeList(excludeList);
			}
		}
		return config;
	}

	public Resource[] getConfigFiles() {
		return configFiles;
	}

	public void setConfigFiles(Resource[] configFiles) {
		this.configFiles = configFiles;
	}
}
