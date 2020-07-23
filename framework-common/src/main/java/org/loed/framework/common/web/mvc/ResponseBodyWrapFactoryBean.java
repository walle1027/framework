package org.loed.framework.common.web.mvc;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/5/30 上午8:42
 */
public class ResponseBodyWrapFactoryBean implements InitializingBean {
	@Autowired
	private RequestMappingHandlerAdapter adapter;

	@Override
	public void afterPropertiesSet() throws Exception {
		List<HandlerMethodReturnValueHandler> returnValueHandlers = adapter.getReturnValueHandlers();
		adapter.setReturnValueHandlers(decorateHandlers(returnValueHandlers));
	}

	private List<HandlerMethodReturnValueHandler> decorateHandlers(List<HandlerMethodReturnValueHandler> handlers) {
		List<HandlerMethodReturnValueHandler> newHanlders = new ArrayList<>();
		for (HandlerMethodReturnValueHandler handler : handlers) {
			if (handler instanceof RequestResponseBodyMethodProcessor) {
				//用自己的ResponseBody包装类替换掉框架的，达到返回Result的效果
				ResponseBodyWrapHandler decorator = new ResponseBodyWrapHandler(handler);
				newHanlders.add(decorator);
			} else {
				newHanlders.add(handler);
			}
		}
		return newHanlders;
	}
}
