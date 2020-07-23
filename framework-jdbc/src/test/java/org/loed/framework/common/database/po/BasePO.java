package org.loed.framework.common.database.po;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/8/13 上午11:18
 */
public class BasePO {
	@Id
	@Column(columnDefinition = "varchar(32) not null comment '主键'")
	private String id;
	@Column(columnDefinition = "varchar(32) comment '创建人'")
	private String createBy;
	@Column(columnDefinition = "varchar(32) comment '修改人'")
	private String updateBy;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
}
