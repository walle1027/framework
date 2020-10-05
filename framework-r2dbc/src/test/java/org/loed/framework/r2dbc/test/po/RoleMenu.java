package org.loed.framework.r2dbc.test.po;

import lombok.Data;

import javax.persistence.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/8/2 6:12 PM
 */
@Table(name = "t_role_menu", indexes = {@Index(name = "udx_role_menu", columnList = "role_id,menu_id",unique = true)})
@Data
public class RoleMenu extends CommonPO {
	@Column
	private Long roleId;
	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role;
	@Column
	private Long menuId;
	@ManyToOne
	@JoinColumn(name = "menu_id")
	private Menu menu;
}
