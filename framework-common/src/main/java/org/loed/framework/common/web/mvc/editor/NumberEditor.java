package org.loed.framework.common.web.mvc.editor;

import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/4/10 21:43
 */
public class NumberEditor extends PropertyEditorSupport {
	private String targetClass;

	public NumberEditor() {
	}

	public NumberEditor(String targetClass) {
		this.targetClass = targetClass;
	}

	@Override
	public String getAsText() {
		Object obj = getValue();
		if (obj == null) {
			return null;
		}
		return obj.toString();
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.isNotEmpty(text)) {
			String plainText = text.trim().replace(" ", "").replace(",", "");
			try {
				switch (targetClass) {
					case "int":
						setValue(Integer.valueOf(plainText));
						break;
					case "long":
						setValue(Long.valueOf(plainText));
						break;
					case "double":
						setValue(Double.valueOf(plainText));
						break;
					case "float":
						setValue(Float.valueOf(plainText));
						break;
				}
			} catch (Exception ignore) {
			}
		}
	}
}
