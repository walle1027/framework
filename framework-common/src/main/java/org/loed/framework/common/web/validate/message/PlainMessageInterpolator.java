package org.loed.framework.common.web.validate.message;


import org.loed.framework.common.web.validate.MessageProvider;

import javax.validation.MessageInterpolator;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.Locale;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-26 上午11:31
 */

public class PlainMessageInterpolator implements MessageInterpolator {
	private MessageProvider messageProvider;

	@Override
	public String interpolate(String s, Context context) {
		ConstraintDescriptor<?> constraintDescriptor = context.getConstraintDescriptor();
		constraintDescriptor.getMessageTemplate();
		return messageProvider.getText(s);
	}

	@Override
	public String interpolate(String s, Context context, Locale locale) {
		return messageProvider.getText(s);
	}

	public MessageProvider getMessageProvider() {
		return messageProvider;
	}

	public void setMessageProvider(MessageProvider messageProvider) {
		this.messageProvider = messageProvider;
	}
}
