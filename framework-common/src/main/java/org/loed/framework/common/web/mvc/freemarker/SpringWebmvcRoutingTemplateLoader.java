//package org.loed.framework.common.web.mvc.freemarker;
//
//import org.loed.framework.common.web.freemarker.FileRoutingTemplateLoader;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.web.context.ServletContextAware;
//
//import javax.servlet.ServletContext;
//
//public class SpringWebmvcRoutingTemplateLoader extends FileRoutingTemplateLoader implements ServletContextAware, InitializingBean {
//
//	private ServletContext servletContext;
//
//	public SpringWebmvcRoutingTemplateLoader() {
//		super();
//	}
//
//	@Override
//	protected String getTemplatePath(String templatePath) {
//		return servletContext.getRealPath(templatePath);
//	}
//
//	@Override
//	public void setServletContext(ServletContext servletContext) {
//		this.servletContext = servletContext;
//	}
//
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		scanRoutingFiles();
//	}
//}
