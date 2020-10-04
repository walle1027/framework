package org.loed.framework.mybatis.test.po;

import lombok.Data;
import org.loed.framework.common.po.TenantId;

import javax.persistence.Column;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/21 9:50 上午
 */
@Data
public class BasePO extends CommonPO {
	@Column(updatable = false)
	@TenantId
	private String tenantId;
}
