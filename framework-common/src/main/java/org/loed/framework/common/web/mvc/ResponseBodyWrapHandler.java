package org.loed.framework.common.web.mvc;

import org.loed.framework.common.Result;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/5/30 上午8:44
 */
public class ResponseBodyWrapHandler implements HandlerMethodReturnValueHandler {
	private final HandlerMethodReturnValueHandler delegate;

	public ResponseBodyWrapHandler(HandlerMethodReturnValueHandler delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return delegate.supportsReturnType(returnType);
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
		if (returnValue == null) {
			delegate.handleReturnValue(new Result<Void>(), returnType, mavContainer, webRequest);
		} else {
			if (!(returnValue instanceof Result)) {
				delegate.handleReturnValue(new Result<>(returnValue), returnType, mavContainer, webRequest);
			} else {
				delegate.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
			}
		}
	}
}
