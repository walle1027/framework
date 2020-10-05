package org.loed.framework;

import org.junit.Before;
import org.loed.framework.common.ServiceLocator;
import org.loed.framework.common.context.SystemContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-29 下午12:36
 */

public class TestBase {
	protected DbUnit dbUnit = new DbUnit();
	@Autowired
	protected ApplicationContext applicationContext;

	@Before
	public void setUp() throws Exception {
		SystemContextHolder.setUserId("wishstar");
		SystemContextHolder.setTenantId("default");
		SystemContextHolder.setUserName("杨涛");
		ServiceLocator.setApplicationContext(applicationContext);
	}
}
