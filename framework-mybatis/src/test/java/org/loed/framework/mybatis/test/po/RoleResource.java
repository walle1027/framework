package org.loed.framework.mybatis.test.po;

import lombok.Data;

import javax.persistence.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/8/2 6:18 PM
 */
@Table(name = "t_role_resource", indexes = {@Index(name = "udx_role_resource", columnList = "role_id,menu_id,resource_id", unique = true)})
@Data
public class RoleResource extends BasePO {
	@Column
	private String roleId;
	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role;
	@Column
	private String menuId;
	@ManyToOne
	@JoinColumn(name = "menu_id")
	private Menu menu;
	@Column
	private String resourceId;
	@ManyToOne
	@JoinColumn(name = "resource_id")
	private Resource resource;
}
