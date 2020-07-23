package org.loed.framework.r2dbc.po;


import org.loed.framework.common.po.BasePO;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name = "t_subject")
public class Subject extends BasePO {

	@Column(columnDefinition = "VARCHAR(128) NULL COMMENT 'code'")
	private String code;

	@Column(columnDefinition = "VARCHAR(128) NULL COMMENT 'siteName'")
	private String siteName;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
}
