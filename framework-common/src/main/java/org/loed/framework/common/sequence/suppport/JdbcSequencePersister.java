package org.loed.framework.common.sequence.suppport;

import org.loed.framework.common.sequence.spi.SequencePersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/3/21.20:52
 */
public class JdbcSequencePersister implements SequencePersister {
	private static final String selectSql = "select last_sequence from t_sys_sequence where sequence_name = ?";
	private static final String updateSql = "update t_sys_sequence set last_sequence = last_sequence + ? where sequence_name=?";
	private static final String deleteSql = "delete from t_sys_sequence where sequence_name=? ";
	private static final String insertSql = "insert into t_sys_sequence(sequence_name,last_sequence)values (?,?)";
	private static final String setOffsetSql = "update t_sys_sequence set last_sequence = ? where sequence_name=?";
	protected final DataSource dataSource;
	protected Logger logger = LoggerFactory.getLogger(getClass());

	public JdbcSequencePersister(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 删除序列
	 *
	 * @param sequenceName 序列名称
	 */
	@Override
	public void setOffset(String sequenceName, long offset) {
		Connection conn = null;
		PreparedStatement psmt = null;
		try {
			conn = getDataSource().getConnection();
			conn.setAutoCommit(true);
			psmt = conn.prepareStatement(setOffsetSql);
			psmt.setLong(1, offset);
			psmt.setString(2, sequenceName);
			int update = psmt.executeUpdate();
			if (update == 0) {
				psmt = conn.prepareStatement(insertSql);
				psmt.setString(1, sequenceName);
				psmt.setLong(2, offset);
				psmt.execute();
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					conn = null;
				}
			}
			if (psmt != null) {
				try {
					psmt.close();
				} catch (SQLException e) {
					psmt = null;
				}
			}
		}
	}

	/**
	 * 取得下一个主键序列号
	 *
	 * @param sequenceName 对象名称
	 * @param step         递增步伐
	 * @return 主键名称
	 */
	@Override
	public Long getNext(String sequenceName, int step) {
		Long lastNumber = 0L;
		try {
			lastNumber = execute(sequenceName, step);
		} catch (SQLException se) {
			String sqlState = se.getSQLState();
			String createSql = null;
			//mysql
			if ("42S02".equals(sqlState)) {
				createSql = "create table t_sys_sequence (\n" +
						" sequence_name varchar(255) not null,\n" +
						" last_sequence bigint not null ,\n" +
						" primary key (sequence_name)\n" +
						") ENGINE=InnoDB default charset=utf8";
			}
			//postgres
			else if ("42P01".equals(sqlState)) {
				createSql = "create table t_sys_sequence(\n" +
						" sequence_name                 varchar(255) not null,\n" +
						" last_sequence               int8 not null,\n" +
						" constraint \"pk_t_sys_sequence\" primary key (\"sequence_name\")\n" +
						")";
			}
			//TODO oracle
			else if ("42000".equals(sqlState)) {
				createSql = "create table t_sys_sequence(\n" +
						" sequence_name                 varchar(255) not null,\n" +
						" last_sequence               int8 not null,\n" +
						" constraint \"pk_t_sys_sequence\" primary key (\"sequence_name\")\n" +
						")";
			}
			//TODO sqlserver
			else if ("??".equals(sqlState)) {

			}
			//TODO 增加其他数据库的
			if (createSql != null) {
				try {
					executeSql(createSql);
					lastNumber = execute(sequenceName, step);
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				logger.error(se.getMessage(), se);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return lastNumber;
	}

	/**
	 * 执行真正的sql
	 *
	 * @param sequenceName 对象名称
	 * @param step         递增步伐
	 * @return 最后的序列号
	 * @throws SQLException
	 */
	private Long execute(String sequenceName, int step) throws SQLException {
		Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		Long result = null;
		Long lastNumber = 0L;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			//TODO 加锁控制
			psmt = conn.prepareStatement(selectSql);
			psmt.setString(1, String.valueOf(sequenceName));
			rs = psmt.executeQuery();
			while (rs.next()) {
				result = rs.getLong(1);
			}
			if (result != null) {
				lastNumber = result;
				psmt = conn.prepareStatement(updateSql);
				psmt.setInt(1, step);
				psmt.setString(2, String.valueOf(sequenceName));
				psmt.executeUpdate();
			} else {
				psmt = conn.prepareStatement(insertSql);
				psmt.setString(1, String.valueOf(sequenceName));
				psmt.setInt(2, step);
				psmt.executeUpdate();
			}
			conn.commit();
			return lastNumber;
		} finally {
			if (psmt != null) {
				try {
					psmt.close();
				} catch (SQLException e) {
					psmt = null;
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					rs = null;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					conn = null;
				}
			}
		}
	}

	private void executeSql(String sql) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(true);
			conn.prepareStatement(sql).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					conn = null;
				}
			}
		}
	}

	/**
	 * 事务回滚
	 *
	 * @param connection jdbc连接
	 */
	private void rollbackConn(Connection connection) {
		if (connection != null) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				connection = null;
			}
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}
}
