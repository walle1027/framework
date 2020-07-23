package org.loed.framework;

import org.junit.Before;
import org.loed.framework.common.ServiceLocator;
import org.loed.framework.common.context.SystemContext;
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
		SystemContext.setUserId("wishstar");
		SystemContext.setTenantCode("default");
		SystemContext.setUserName("杨涛");
		ServiceLocator.setApplicationContext(applicationContext);
	}
}
