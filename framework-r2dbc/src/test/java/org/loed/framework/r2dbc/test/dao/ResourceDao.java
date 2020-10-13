package org.loed.framework.r2dbc.test.dao;

import org.loed.framework.r2dbc.dao.R2dbcDao;
import org.loed.framework.r2dbc.test.po.Resource;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/10/12 2:17 下午
 */
public interface ResourceDao extends R2dbcDao<Resource, String> {
}
