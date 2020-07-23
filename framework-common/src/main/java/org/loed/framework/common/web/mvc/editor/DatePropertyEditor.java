package org.loed.framework.common.web.mvc.editor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015-12-4 15:14
 */

public class DatePropertyEditor extends PropertyEditorSupport {

	@Override
	public String getAsText() {
		Object obj = getValue();
		if (obj == null) {
			return null;
		}
		Date date = (Date) obj;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.isNotEmpty(text)) {
			try {
				Date date = DateUtils.parseDate(text, new String[]{
						"yyyy-MM-dd HH:mm:ss"
						, "yyyy-MM-dd HH:mm"
						, "yyyy-MM-dd HH"
						, "yyyy-MM-dd"
						, "yyyy-MM"
						, "yyyy"
				});
				setValue(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
