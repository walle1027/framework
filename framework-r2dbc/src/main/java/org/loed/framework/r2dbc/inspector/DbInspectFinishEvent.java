package org.loed.framework.r2dbc.inspector;

import org.springframework.context.ApplicationEvent;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 3:06 下午
 */
public class DbInspectFinishEvent extends ApplicationEvent {
	public DbInspectFinishEvent(Object source) {
		super(source);
	}
}
