package org.loed.framework.r2dbc.test.dao;

import org.loed.framework.r2dbc.dao.R2dbcDao;
import org.loed.framework.r2dbc.test.po.LongIdWithDeleted;

import java.math.BigInteger;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/30 11:03 上午
 */
public interface LongIdWithDeletedDao extends R2dbcDao<LongIdWithDeleted, BigInteger> {
}
