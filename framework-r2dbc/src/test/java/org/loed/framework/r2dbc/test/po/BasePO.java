package org.loed.framework.r2dbc.test.po;

import lombok.Data;

import javax.persistence.Column;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/21 9:50 上午
 */
@Data
public class BasePO extends CommonPO {
	@Column
	private String tenantId;
}
