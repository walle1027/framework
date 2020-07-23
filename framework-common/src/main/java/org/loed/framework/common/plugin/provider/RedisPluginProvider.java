//package org.loed.framework.common.plugin.provider;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import org.apache.commons.lang3.StringUtils;
//import org.loed.framework.common.plugin.Plugin;
//import org.loed.framework.common.plugin.PluginProtocol;
//import org.loed.framework.common.plugin.PluginProvider;
//import org.loed.framework.common.plugin.impl.HttpPlugin;
//import org.loed.framework.common.plugin.impl.InternalPlugin;
//import org.loed.framework.common.plugin.impl.SoaPlugin;
//import org.loed.framework.common.util.SerializeUtils;
//
//import java.util.Map;
//
///**
// * @author Thomason
// * @version 1.0
// * @since 2016/11/28 11:21
// */
//
//public class RedisPluginProvider implements PluginProvider {
//	//缓存前缀
//	protected String prefix = "plugin";
//	//key之间的分隔符
//	protected String separator = ":";
//	//缓存门面
//	protected RedisCache redisCache;
//
//	@Override
//	public boolean hasPlugin(String tenantCode, String signature) {
//		String redisKey = prefix + separator + tenantCode + separator + signature;
//		return redisCache.containsKey(redisKey);
//	}
//
//	@Override
//	public Plugin getPlugin(String tenantCode, String signature) {
//		String field = redisCache.hget(signature, tenantCode);
//		if (StringUtils.isNotBlank(field)) {
//			return buildPlugin(field);
//		}
//		String field1 = redisCache.hget(signature, ALL_SIGN);
//		if (StringUtils.isNotBlank(field)) {
//			return buildPlugin(field1);
//		}
//		return null;
//	}
//
//	private Plugin buildPlugin(String jsonString) {
//		Map<String,Object> map = SerializeUtils.fromJson(jsonString, new TypeReference<Map<String,Object>>() {
//		});
//		String protocol = (String) map.get("protocol");
//		if (PluginProtocol.http.name().equals(protocol)) {
//			return SerializeUtils.fromJson(jsonString, HttpPlugin.class);
//		} else if (PluginProtocol.soa.name().equals(protocol)) {
//			return SerializeUtils.fromJson(jsonString, SoaPlugin.class);
//		} else if (PluginProtocol.internal.name().equals(protocol)) {
//			return SerializeUtils.fromJson(jsonString, InternalPlugin.class);
//		}
//		return null;
//	}
//
//	public void setRedisCache(RedisCache redisCache) {
//		this.redisCache = redisCache;
//	}
//}
