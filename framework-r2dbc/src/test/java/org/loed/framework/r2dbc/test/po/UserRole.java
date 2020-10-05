package org.loed.framework.r2dbc.test.po;

import lombok.Data;

import javax.persistence.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/8/2 6:11 PM
 */
@Table(name = "t_user_role", indexes = {@Index(name = "udx_user_role", columnList = "user_id,role_id", unique = true)})
@Data
public class UserRole extends CommonPO {
	@Column
	private Long userId;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	@Column
	private Long roleId;
	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role;
}
