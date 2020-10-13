package org.loed.framework.r2dbc.test.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/8/2 6:11 PM
 */
@Table(name = "t_role",indexes = {@Index(name = "uk_role",columnList = "role_code",unique = true)})
@Data
public class Role extends CommonPO {
	@Column(length = 20)
	private String roleCode;
	@Column(length = 200)
	private String roleName;
	@Column(columnDefinition = "text")
	private String description;
	@OneToMany
	private List<RoleMenu> roleMenuList;
	@OneToMany
	private List<UserRole> userRoleList;
	@OneToMany
	private List<RoleResource> resourceList;
}
