package org.loed.framework.r2dbc.test;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.common.util.UUIDUtils;
import org.loed.framework.r2dbc.test.dao.*;
import org.loed.framework.r2dbc.test.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/10/12 2:15 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = R2dbcApplication.class)
public class JoinTest {
	@Autowired
	private MenuDao menuDao;
	@Autowired
	private ResourceDao resourceDao;
	@Autowired
	private RoleDao roleDao;
	@Autowired
	private RoleMenuDao roleMenuDao;
	@Autowired
	private RoleResourceDao roleResourceDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserRoleDao userRoleDao;

	private SystemContext systemContext;

	@Before
	public void setUp() throws Exception {
		systemContext = new SystemContext();
		systemContext.setAccountId("r2dbc_test_account_id");
		systemContext.setTenantId("r2dbc_test_tenant_id");
		systemContext.setUserId("r2dbc_test_user_id");
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
		menuDao.batchInsert(menuList).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext)).blockLast();
		resourceDao.batchInsert(resourceList).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext)).blockLast();

		//add user
		User user = new User();
		user.setUsername("testAdminUser");
		user.setAccount("testAdminUser");
		user.setPassword("testPassword");
		user.setIsLocked(0);
		user.setMobile("testMobile");
		user.setEmail("test@test.com");
		userDao.insert(user).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext)).block();
		//add role
		Role role = new Role();
		role.setRoleCode("testAdminRole");
		role.setRoleName("testAdminRole");
		roleDao.insert(role).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext)).block();

		//add role menu and role resource
		for (Resource resource : resourceList) {
			RoleResource roleResource = new RoleResource();
			roleResource.setMenuId(resource.getMenuId());
			roleResource.setRoleId(role.getId());
			roleResource.setResourceId(resource.getId());
			roleResourceDao.insert(roleResource).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext)).block();
		}
		for (Menu menu : menuList) {
			RoleMenu roleMenu = new RoleMenu();
			roleMenu.setMenuId(menu.getId());
			roleMenu.setRoleId(role.getId());
			roleMenuDao.insert(roleMenu).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext)).block();
		}

		//user role
		UserRole userRole = new UserRole();
		userRole.setRoleId(role.getId());
		userRole.setUserId(user.getId());
		userRoleDao.insert(userRole).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext)).block();
	}

	@Test
	public void testInnerJoin() {
		Criteria<RoleMenu> criteria = Criteria.from(RoleMenu.class)
				.innerJoin(RoleMenu::getRole).innerJoin(Role::getUserRoleList).innerJoin(UserRole::getUser).and(User::getAccount).is("testAdminUser")
				.innerJoin(RoleMenu::getRole).and(Role::getRoleCode).is("testAdminRole")
				.innerJoin(RoleMenu::getMenu).and(Menu::getId).isNotNull()
				.innerJoin(RoleMenu::getRole).innerJoin(Role::getResourceList).innerJoin(RoleResource::getResource)
				.and(Resource::getId).isNotNull();
		Mono<List<RoleMenu>> mono = roleMenuDao.find(criteria).collectList();
		StepVerifier.create(mono.log()).expectNextMatches(roleMenus -> {
			for (RoleMenu roleMenu : roleMenus) {
				System.out.println(SerializeUtils.toJson(roleMenu));
			}
			return true;
		}).verifyComplete();
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
		Mono<List<RoleMenu>> listMono = roleMenuDao.find(criteria).collectList();
		StepVerifier.create(listMono.log()).expectNextMatches(roleMenus -> {
			for (RoleMenu roleMenu : roleMenus) {
				Assert.assertNull(roleMenu.getId());
				Assert.assertNotNull(roleMenu.getRoleId());
				Assert.assertNotNull(roleMenu.getMenuId());
				Assert.assertNotNull(roleMenu.getMenu().getId());
				Assert.assertTrue(StringUtils.startsWith(roleMenu.getMenu().getMenuCode(), "testMenu"));
				Assert.assertTrue(StringUtils.startsWith(roleMenu.getMenu().getMenuName(), "testMenu"));
			}
			return true;
		}).verifyComplete();
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
						Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuName))
				.excludes(RoleMenu::getRoleId, RoleMenu::getMenuId)
				.asc(Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuCode))
				.desc(Criteria.CascadeProperty.from(RoleMenu::getMenu).next(Menu::getMenuName));
		Mono<List<RoleMenu>> listMono = roleMenuDao.find(criteria).collectList();
		StepVerifier.create(listMono.log()).expectNextMatches(roleMenus -> {
			int index = 0;
			for (int i = 0; i < roleMenus.size(); i++) {
				RoleMenu roleMenu = roleMenus.get(i);
				Assert.assertNotNull(roleMenu.getId());
				Assert.assertNull(roleMenu.getRoleId());
				Assert.assertNull(roleMenu.getMenuId());
				Assert.assertNull(roleMenu.getMenu().getId());
				int j = Integer.parseInt(roleMenu.getMenu().getMenuCode().substring("testMenu".length()));
				Assert.assertTrue(j >= index);
				index = j;
				Assert.assertNull(roleMenu.getMenu().getMenuName());
				Assert.assertEquals(roleMenu.getMenu().getDescription(), "testMenu");
			}
			return true;
		}).verifyComplete();
	}

	@After
	public void tearDown() throws Exception {
		userDao.execute("truncate table t_user", null).block();
		userDao.execute("truncate table t_role", null).block();
		userDao.execute("truncate table t_role_menu", null).block();
		userDao.execute("truncate table t_user_role", null).block();
		userDao.execute("truncate table t_role_resource", null).block();
		userDao.execute("truncate table t_resource", null).block();
		userDao.execute("truncate table t_menu", null).block();
	}
}
