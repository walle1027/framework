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
@Table(name = "t_menu",indexes = {@Index(name = "uk_menu",columnList = "app_code,menu_code")})
@Data
public class Menu extends CommonPO {
	@Column(length = 20)
	private String appCode;
	@Column(length = 20)
	private String menuCode;
	@Column(length = 200)
	private String menuName;
	@Column(length = 20)
	private String menuType;
	@Column()
	private String menuUrl;
	@Column(length = 200)
	private String icon;
	@Column(columnDefinition = "tinyint default 0")
	private Boolean hideChildrenInMenu;
	@Column(columnDefinition = "tinyint default 0")
	private Boolean hideInMenu;
	@Column(columnDefinition = "text")
	private String description;
	@Column()
	private Integer menuLevel;
	@Column()
	private Double orderNo;
	@Column()
	private Long parentId;
	@Column(columnDefinition = "text")
	private String idPath;
	@OneToMany
	private List<RoleMenu> roleMenuList;
}
