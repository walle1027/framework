package org.loed.framework.common.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.context.SystemContextHolder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/15 5:04 下午
 */
@Slf4j
public class SystemContextAwareMessageConverter implements MessageConverter {

	private final MessageConverter delegate;

	public SystemContextAwareMessageConverter(MessageConverter delegate) {
		this.delegate = delegate;
	}

	@Override
	public Message toMessage(Object o, MessageProperties messageProperties) throws MessageConversionException {
		Message message = delegate.toMessage(o, messageProperties);
		//add SystemContext
		SystemContext systemContext = SystemContextHolder.getSystemContext();
		List<Pair<String, String>> headers = systemContext.toHeaders();
		if (CollectionUtils.isNotEmpty(headers)) {
			Map<String, Object> messageHeaders = message.getMessageProperties().getHeaders();
			for (Pair<String, String> header : headers) {
				messageHeaders.put(header.getKey(), header.getValue());
			}
		}
		return message;
	}

	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		String messageMeta = message.getMessageProperties().getAppId() + ":" + message.getMessageProperties().getClusterId() + ":" + message.getMessageProperties().getConsumerQueue();
		Object object = delegate.fromMessage(message);
		Map<String, Object> headers = message.getMessageProperties().getHeaders();
		SystemContext systemContext = new SystemContext();
		for (Map.Entry<String, Object> entry : headers.entrySet()) {
			String headerKey = entry.getKey();
			Object value = entry.getValue();
			if (StringUtils.startsWithIgnoreCase(headerKey, SystemContext.CONTEXT_PREFIX)) {
				if (StringUtils.equalsIgnoreCase(headerKey, SystemContext.CONTEXT_ACCOUNT_ID)) {
					systemContext.setAccountId((String) value);
					if (log.isDebugEnabled()) {
						log.debug("message {} has  header: {}, with value {}", messageMeta, SystemContext.CONTEXT_ACCOUNT_ID, value);
					}
				} else if (StringUtils.equalsIgnoreCase(headerKey, SystemContext.CONTEXT_ACCOUNT_NAME)) {
					try {
						String headerVal = URLDecoder.decode((String) value, "utf-8");
						systemContext.setAccountName(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("message {} has  header: {}, with value {}", messageMeta, SystemContext.CONTEXT_ACCOUNT_NAME, headerVal);
						}
					} catch (UnsupportedEncodingException e) {
						log.error(e.getMessage(), e);
					}

				} else if (StringUtils.equalsIgnoreCase(headerKey, SystemContext.CONTEXT_USER_ID)) {
					systemContext.setUserId((String) value);
					if (log.isDebugEnabled()) {
						log.debug("message {} has  header: {}, with value {}", messageMeta, SystemContext.CONTEXT_USER_ID, value);
					}
				} else if (StringUtils.equalsIgnoreCase(headerKey, SystemContext.CONTEXT_USER_NAME)) {
					try {
						String headerVal = URLDecoder.decode((String) value, "utf-8");
						systemContext.setUserName(headerVal);
						if (log.isDebugEnabled()) {
							log.debug("message {} has  header: {}, with value {}", messageMeta, SystemContext.CONTEXT_USER_NAME, headerVal);
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

				} else if (StringUtils.equalsIgnoreCase(headerKey, SystemContext.CONTEXT_CLIENT_IP)) {
					systemContext.setClientIp((String) value);
					if (log.isDebugEnabled()) {
						log.debug("message {} has  header: {}, with value {}", messageMeta, SystemContext.CONTEXT_CLIENT_IP, value);
					}
				} else if (StringUtils.equalsIgnoreCase(headerKey, SystemContext.CONTEXT_USER_AGENT)) {
					systemContext.setUserAgent((String) value);
					if (log.isDebugEnabled()) {
						log.debug("message {} has  header: {}, with value {}", messageMeta, SystemContext.CONTEXT_USER_AGENT, value);
					}
				} else if (StringUtils.equalsIgnoreCase(headerKey, SystemContext.CONTEXT_TENANT_CODE)) {
					systemContext.setTenantCode((String) value);
					if (log.isDebugEnabled()) {
						log.debug("message {} has  header: {}, with value {}", messageMeta, SystemContext.CONTEXT_TENANT_CODE, value);
					}
				} else if (StringUtils.equalsIgnoreCase(headerKey, SystemContext.CONTEXT_LOCALE)) {
					systemContext.setLocale((String) value);
					if (log.isDebugEnabled()) {
						log.debug("message {} has  header: {}, with value {}", messageMeta, SystemContext.CONTEXT_LOCALE, value);
					}
				}
				//TODO add more
				else {
					systemContext.set(headerKey, (String) value);
				}
			}
			SystemContextHolder.set(systemContext);
		}
		return object;
	}
}
