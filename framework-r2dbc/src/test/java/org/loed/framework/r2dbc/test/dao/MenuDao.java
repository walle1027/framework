package org.loed.framework.r2dbc.test.dao;

import org.loed.framework.r2dbc.dao.R2dbcDao;
import org.loed.framework.r2dbc.query.Query;
import org.loed.framework.r2dbc.test.po.Menu;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/9/19 5:57 PM
 */
public interface MenuDao extends R2dbcDao<Menu, Long> {
	/**
	 * 获取一个菜单下最大的顺序
	 *
	 * @param parentId 上级菜单Id
	 * @return 最大序号
	 */
	@Query("select max(order_no) from t_menu where parent_id = :parentId")
	Mono<Double> getMaxOrder(Long parentId);
}
