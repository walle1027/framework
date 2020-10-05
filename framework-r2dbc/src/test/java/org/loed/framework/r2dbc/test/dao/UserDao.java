package org.loed.framework.r2dbc.test.dao;


import org.loed.framework.r2dbc.dao.R2dbcDao;
import org.loed.framework.r2dbc.test.po.User;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/9/12 4:55 PM
 */
public interface UserDao extends R2dbcDao<User,Long> {
}
