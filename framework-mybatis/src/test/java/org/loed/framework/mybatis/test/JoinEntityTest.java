package org.loed.framework.mybatis.test;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.common.util.UUIDUtils;
import org.loed.framework.mybatis.test.mapper.*;
import org.loed.framework.mybatis.test.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/27 3:44 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MybatisTestApplication.class)
public class JoinEntityTest {
	@Autowired
	private MenuMapper menuMapper;
	@Autowired
	private ResourceMapper resourceMapper;
	@Autowired
	private RoleMapper roleMapper;
	@Autowired
	private RoleMenuMapper roleMenuMapper;
	@Autowired
	private RoleResourceMapper roleResourceMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserRoleMapper userRoleMapper;

	@Before
	public void setUp() throws Exception {
		SystemContextHolder.setTenantCode("mybatis_test_tenant");
		SystemContextHolder.setAccountId("mybatis_test_accountId");
		SystemContextHolder.setUserId("mybatis_test_userId");
		//TODO 准备数据
		//10个菜单
		List<Menu> menuList = new ArrayList<>();
		List<Resource> resourceList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Menu menu = new Menu();
			menu.setAppCode("test");
			menu.setMenuCode("testMenu" + i);
			menu.setMenuName("testMenu" + i);
			menu.setDescription("testMenu");
			menu.setMenuLevel(1);
			menu.setId(UUIDUtils.getUUID());
			menu.setIdPath(menu.getId());
			menu.setMenuType("web");
			menu.setOrderNo(i * 1d);
			menu.setMenuUrl(null);
			for (int j = 0; j < 5; j++) {
				Resource resource = new Resource();
				resource.setComponentType("Button");
				resource.setHtmlTagName("button");
				resource.setResourceCode("menu" + i + "_resource" + j);
				resource.setResourceName("menu" + i + "_resource" + j);
				resource.setMenuId(menu.getId());
				resourceList.add(resource);
			}
			menuList.add(menu);
		}
		menuMapper.batchInsert(menuList);
		resourceMapper.batchInsert(resourceList);

		//add user
		User user = new User();
		user.setUsername("testAdminUser");
		user.setAccount("testAdminUser");
		user.setPassword("testPassword");
		user.setIsLocked(0);
		user.setMobile("testMobile");
		user.setEmail("test@test.com");
		userMapper.insert(user);
		//add role
		Role role = new Role();
		role.setRoleCode("testAdminRole");
		role.setRoleName("testAdminRole");
		roleMapper.insert(role);

		//add role menu and role resource
		for (Resource resource : resourceList) {
			RoleResource roleResource = new RoleResource();
			roleResource.setMenuId(resource.getMenuId());
			roleResource.setRoleId(role.getId());
			roleResource.setResourceId(resource.getId());
			roleResourceMapper.insert(roleResource);
		}
		for (Menu menu : menuList) {
			RoleMenu roleMenu = new RoleMenu();
			roleMenu.setMenuId(menu.getId());
			roleMenu.setRoleId(role.getId());
			roleMenuMapper.insert(roleMenu);
		}

		//user role
		UserRole userRole = new UserRole();
		userRole.setRoleId(role.getId());
		userRole.setUserId(user.getId());
		userRoleMapper.insert(userRole);
	}

	@Test
	public void testInnerJoin() {
		Criteria<RoleMenu> criteria = Criteria.from(RoleMenu.class)
				.innerJoin(RoleMenu::getRole).innerJoin(Role::getUserRoleList).innerJoin(UserRole::getUser).and(User::getAccount).is("testAdminUser")
				.innerJoin(RoleMenu::getRole).and(Role::getRoleCode).is("testAdminRole")
				.innerJoin(RoleMenu::getMenu).and(Menu::getId).isNotNull()
				.innerJoin(RoleMenu::getRole).innerJoin(Role::getResourceList).innerJoin(RoleResource::getResource)
				.and(Resource::getId).isNotNull();
		List<RoleMenu> roleMenus = roleMenuMapper.find(criteria);
		for (RoleMenu roleMenu : roleMenus) {
			System.out.println(SerializeUtils.toJson(roleMenu));
		}
	}

	@Test
	public void testLeftJoinAndIncludes() {
		Criteria<RoleMenu> criteria = Criteria.from(RoleMenu.class)
				.leftJoin(RoleMenu::getRole).innerJoin(Role::getUserRoleList).innerJoin(UserRole::getUser).and(User::getAccount).is("testAdminUser")
				.leftJoin(RoleMenu::getRole).and(Role::getRoleCode).is("testAdminRole")
				.innerJoin(RoleMenu::getMenu).and(Menu::getId).isNotNull()
				.leftJoin(RoleMenu::getRole).innerJoin(Role::getResourceList).innerJoin(RoleResource::getResource)
				.and(Resource::getId).isNotNull()
				.includes(Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getId),
						Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuCode),
						Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuName))
				.includes(RoleMenu::getRoleId, RoleMenu::getMenuId)
				.asc(Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuCode))
				.desc(Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuName));
		List<RoleMenu> roleMenus = roleMenuMapper.find(criteria);
		int index = 0;
		for (RoleMenu roleMenu : roleMenus) {
			Assert.assertNull(roleMenu.getId());
			Assert.assertNotNull(roleMenu.getRoleId());
			Assert.assertNotNull(roleMenu.getMenuId());
			Assert.assertNotNull(roleMenu.getMenu().getId());
			int j = Integer.parseInt(roleMenu.getMenu().getMenuCode().substring("testMenu".length()));
			Assert.assertTrue(j >= index);
			index = j;
			Assert.assertTrue(StringUtils.startsWith(roleMenu.getMenu().getMenuCode(), "testMenu"));
			Assert.assertTrue(StringUtils.startsWith(roleMenu.getMenu().getMenuName(), "testMenu"));
		}
	}

	@Test
	public void testLeftJoinAndExcludes() {
		Criteria<RoleMenu> criteria = Criteria.from(RoleMenu.class)
				.leftJoin(RoleMenu::getRole).innerJoin(Role::getUserRoleList).innerJoin(UserRole::getUser).and(User::getAccount).is("testAdminUser")
				.leftJoin(RoleMenu::getRole).and(Role::getRoleCode).is("testAdminRole")
				.innerJoin(RoleMenu::getMenu).and(Menu::getId).isNotNull()
				.leftJoin(RoleMenu::getRole).innerJoin(Role::getResourceList).innerJoin(RoleResource::getResource)
				.and(Resource::getId).isNotNull()
				.excludes(Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getId),
						Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuCode),
						Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuName))
				.excludes(RoleMenu::getRoleId, RoleMenu::getMenuId)
				.asc(Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuCode))
				.desc(Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuName));
		List<RoleMenu> roleMenus = roleMenuMapper.find(criteria);
		int index = 0;
		for (RoleMenu roleMenu : roleMenus) {
			Assert.assertNotNull(roleMenu.getId());
			Assert.assertNull(roleMenu.getRoleId());
			Assert.assertNull(roleMenu.getMenuId());
			Assert.assertNull(roleMenu.getMenu().getId());
//			int j = Integer.parseInt(roleMenu.getMenu().getMenuCode().substring("testMenu".length()));
//			Assert.assertTrue(j >= index);
//			index = j;
			Assert.assertNull(roleMenu.getMenu().getMenuCode());
			Assert.assertNull(roleMenu.getMenu().getMenuName());
			Assert.assertEquals(roleMenu.getMenu().getDescription(), "testMenu");
		}
	}

	@After
	public void tearDown() throws Exception {
		userMapper.execute("truncate table t_user", null);
		userMapper.execute("truncate table t_role", null);
		userMapper.execute("truncate table t_role_menu", null);
		userMapper.execute("truncate table t_user_role", null);
		userMapper.execute("truncate table t_role_resource", null);
		userMapper.execute("truncate table t_resource", null);
		userMapper.execute("truncate table t_menu", null);
	}
}
