package org.loed.framework.common.plugin.impl;

import org.loed.framework.common.plugin.Plugin;
import org.loed.framework.common.plugin.PluginProtocol;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/11/28 10:27
 */

public class SoaPlugin extends Plugin {
	@Override
	public String getProtocol() {
		return PluginProtocol.soa.name();
	}
}
