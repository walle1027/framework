package org.loed.framework.common.web.mvc;

import org.loed.framework.common.query.Pagination;
import org.loed.framework.common.web.mvc.editor.BigDecimalPropertyEditor;
import org.loed.framework.common.web.mvc.editor.DatePropertyEditor;
import org.loed.framework.common.web.mvc.editor.NumberEditor;
import org.loed.framework.common.web.mvc.editor.PagePropertyEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.support.WebBindingInitializer;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015-12-4 15:51
 */

public class WebBindingInitial implements WebBindingInitializer {
	@Override
	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.registerCustomEditor(Date.class, new DatePropertyEditor());
		webDataBinder.registerCustomEditor(BigDecimal.class, new BigDecimalPropertyEditor());
		webDataBinder.registerCustomEditor(Long.class, new NumberEditor("long"));
		webDataBinder.registerCustomEditor(Integer.class, new NumberEditor("int"));
		webDataBinder.registerCustomEditor(Double.class, new NumberEditor("double"));
		webDataBinder.registerCustomEditor(Float.class, new NumberEditor("float"));
		webDataBinder.registerCustomEditor(Pagination.class, new PagePropertyEditor());
	}
}
