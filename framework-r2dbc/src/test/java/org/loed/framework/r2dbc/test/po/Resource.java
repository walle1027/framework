package org.loed.framework.r2dbc.test.po;

import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/8/2 6:12 PM
 */
@Table(name = "t_resource",indexes = {@Index(name = "uk_resource",columnList = "menu_id,resource_code")})
@Data
public class Resource extends BasePO {
	@Column
	private String menuId;
	@ManyToOne
	@JoinColumn(name = "menu_id")
	private Menu menu;
	@Column(length = 20)
	private String resourceCode;
	@Column(length = 255)
	private String resourceName;
	@Column(length = 255)
	private String htmlTagName;
	@Column(length = 255)
	private String componentType;
	@Column
	private Integer orderNumber;
	@OneToMany
	private List<RoleResource> roleResourceList;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		Resource resource = (Resource) o;
		return Objects.equals(getId(), resource.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
