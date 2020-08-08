package org.loed.framework.mybatis.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.util.DataType;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.mybatis.MybatisSqlBuilder;

import javax.persistence.GenerationType;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/2/20 3:16 PM
 */
@Intercepts(
		{
				@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
		}
)
public class InsertInterceptor extends BasePreProcessInterceptor<Boolean> {
	@Override
	protected Object doIntercept(Invocation invocation, Boolean context) throws Throwable {
		Object[] args = invocation.getArgs();
		Object object = args[1];
		Executor executor = (Executor) invocation.getTarget();
		Table table = ORMapping.get(object.getClass());

		List<Column> insertList = table.getColumns().stream().filter(Column::isInsertable).collect(Collectors.toList());
		Connection connection = executor.getTransaction().getConnection();
		StringBuilder builder = new StringBuilder();
		builder.append("insert into ");
		builder.append(MybatisSqlBuilder.getTableNameByPO(table, object));
		builder.append(MybatisSqlBuilder.BLANK).append("(");
		insertList.forEach(column -> {
			builder.append(MybatisSqlBuilder.BLANK).append(column.getSqlName()).append(",");
		});
		builder.deleteCharAt(builder.length() - 1);
		builder.append(")").append(MybatisSqlBuilder.BLANK).append("values(");
		insertList.forEach(column -> {
			builder.append("?").append(",");
		});
		builder.deleteCharAt(builder.length() - 1);
		builder.append(")");
		if (logger.isDebugEnabled()) {
			logger.debug(builder.toString());
		}
		int rows = 0;
		Column pkColumn = table.getColumns().stream().filter(Column::isPk).findFirst().orElse(null);
		boolean generatedId = pkColumn != null && GenerationType.AUTO.equals(pkColumn.getIdGenerationType());
		PreparedStatement ps;
		if (generatedId) {
			ps = connection.prepareStatement(builder.toString(), Statement.RETURN_GENERATED_KEYS);
		} else {
			ps = connection.prepareStatement(builder.toString());
		}
		try {
			for (int i = 0; i < insertList.size(); i++) {
				Column column = insertList.get(i);
				if (column.isVersioned()) {
					ps.setObject(i + 1, 0, column.getSqlType());
				} else {
					//处理 boolean型
					int dataType = DataType.getDataType(column.getJavaType());
					if ((DataType.DT_Boolean == dataType || DataType.DT_boolean == dataType)
							&& JDBCType.INTEGER.getVendorTypeNumber() == column.getSqlType()) {
						Object value = ReflectionUtils.getFieldValue(object, column.getJavaName());
						Boolean booleanValue = (Boolean) DataType.toType(value, DataType.DT_Boolean);
						if (booleanValue != null) {
							ps.setObject(i + 1, booleanValue ? 1 : 0, column.getSqlType());
						} else {
							ps.setObject(i + 1, null, column.getSqlType());
						}
					} else {
						ps.setObject(i + 1, ReflectionUtils.getFieldValue(object, column.getJavaName()), column.getSqlType());
					}
				}
			}
			rows = ps.executeUpdate();
			if (generatedId) {
				ResultSet generatedKeys = ps.getGeneratedKeys();
				while (generatedKeys.next()) {
					ReflectionUtils.setFieldValue(object, pkColumn.getJavaName(), generatedKeys.getLong(1));
				}
			}
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception ignored) {
				}
			}
			executor.clearLocalCache();
		}
		return rows;
	}

	@Override
	protected Boolean preProcess(Invocation invocation) {
		final Object[] args = invocation.getArgs();
		final MappedStatement ms = (MappedStatement) args[0];
		SqlSource sqlSource = ms.getSqlSource();
		BoundSql boundSql = sqlSource.getBoundSql(args[1]);
		String sql = boundSql.getSql();
		return MybatisSqlBuilder.INSERT.equals(sql);
	}

	@Override
	protected boolean shouldApply(Invocation invocation, Boolean context) {
		return context;
	}

	@Override
	public String getName() {
		return "insert interceptor";
	}

	@Override
	public void setProperties(Properties properties) {

	}
}
