package org.loed.framework.common.web.mvc.editor;

import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015-12-4 15:33
 */

public class BigDecimalPropertyEditor extends PropertyEditorSupport {
	@Override
	public String getAsText() {
		Object obj = getValue();
		if (obj == null) {
			return null;
		}
		BigDecimal decimal = (BigDecimal) obj;
		return decimal.toPlainString();
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.isNotEmpty(text)) {
			String plainText = text.trim().replace(" ", "").replace(",", "");
			try {
				setValue(new BigDecimal(plainText));
			} catch (Exception ignore) {
			}
		}
	}
}
