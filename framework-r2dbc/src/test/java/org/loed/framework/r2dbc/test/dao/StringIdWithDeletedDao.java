package org.loed.framework.r2dbc.test.dao;

import org.loed.framework.r2dbc.dao.R2dbcDao;
import org.loed.framework.r2dbc.test.po.StringIdWithDeleted;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/30 11:05 上午
 */
public interface StringIdWithDeletedDao extends R2dbcDao<StringIdWithDeleted,String> {
}
