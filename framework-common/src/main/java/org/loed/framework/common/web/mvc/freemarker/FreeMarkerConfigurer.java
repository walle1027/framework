//package org.loed.framework.common.web.mvc.freemarker;
//
//import freemarker.template.Configuration;
//import freemarker.template.TemplateException;
//
//import java.io.IOException;
//
///**
// * @author Thomason
// * @version 1.0
// * @since 2016/9/19 15:22
// */
//
//public class FreeMarkerConfigurer extends org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer {
//	private boolean localizedLookup = false;
//	private boolean whitespaceStripping = true;
//	private String defaultEncoding = "utf-8";
//
//	@Override
//	protected void postProcessConfiguration(Configuration config) throws IOException, TemplateException {
//		config.setLocalizedLookup(localizedLookup);
//		config.setWhitespaceStripping(whitespaceStripping);
//		config.setDefaultEncoding(defaultEncoding);
//	}
//
//	public void setLocalizedLookup(boolean localizedLookup) {
//		this.localizedLookup = localizedLookup;
//	}
//
//	public void setWhitespaceStripping(boolean whitespaceStripping) {
//		this.whitespaceStripping = whitespaceStripping;
//	}
//
//	@Override
//	public void setDefaultEncoding(String defaultEncoding) {
//		this.defaultEncoding = defaultEncoding;
//	}
//}
