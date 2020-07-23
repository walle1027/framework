package org.loed.framework.common.web.mvc.editor;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.query.Pagination;

import java.beans.PropertyEditorSupport;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/10/5 0:53
 */

public class PagePropertyEditor extends PropertyEditorSupport {
	@Override
	public String getAsText() {
		return getValue().toString();
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.isNotEmpty(text)) {
			Pagination pagination = new Pagination();
			pagination.setPageNo(Integer.valueOf(text));
			setValue(pagination);
		}
	}
}
