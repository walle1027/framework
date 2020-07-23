package org.loed.framework.common.web.mvc.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/3/3.14:51
 */
@WebListener
public class WebRecycleListener implements ServletContextListener {

	private Logger logger = LoggerFactory.getLogger(WebRecycleListener.class);

	private static List<Closeable> closeableList = new ArrayList<>();

	public static void register(Closeable closeable) {
		closeableList.add(closeable);
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println(WebRecycleListener.class.getSimpleName() + " initialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		for (Closeable closeable : closeableList) {
			try {
				closeable.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
