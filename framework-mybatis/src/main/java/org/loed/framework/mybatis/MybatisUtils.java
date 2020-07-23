package org.loed.framework.mybatis;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;

public class MybatisUtils {
	public static MappedStatement copyFromMappedStatement(MappedStatement ms, String suffix, SqlSource newSqlSource) {
		MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + suffix, newSqlSource, ms.getSqlCommandType());

		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
			StringBuilder keyProperties = new StringBuilder();
			for (String keyProperty : ms.getKeyProperties()) {
				keyProperties.append(keyProperty).append(",");
			}
			keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
			builder.keyProperty(keyProperties.toString());
		}

		//setStatementTimeout()
		builder.timeout(ms.getTimeout());

		//setStatementResultMap()
		builder.parameterMap(ms.getParameterMap());

		//setStatementResultMap()
		builder.resultMaps(ms.getResultMaps());
		builder.resultSetType(ms.getResultSetType());

		//setStatementCache()
		builder.cache(ms.getCache());
		builder.flushCacheRequired(ms.isFlushCacheRequired());
		builder.useCache(ms.isUseCache());

		return builder.build();
	}

	public static MappedStatement copyFromNewSql(MappedStatement ms, BoundSql boundSql,
	                                             String sql) {
		BoundSql newBoundSql = copyFromBoundSql(ms, boundSql, sql, boundSql.getParameterMappings(), boundSql.getParameterObject());
		SqlSource sqlSource = new BoundSqlSqlSource(newBoundSql);
//				new StaticSqlSource(ms.getConfiguration(), sql, boundSql.getParameterMappings());
		return copyFromMappedStatement(ms, "", sqlSource);
	}

	public static BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql,
	                                        String sql, List<ParameterMapping> parameterMappings, Object parameter) {
		BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, parameterMappings, parameter);
		for (ParameterMapping mapping : boundSql.getParameterMappings()) {
			String prop = mapping.getProperty();
			if (boundSql.hasAdditionalParameter(prop)) {
				newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
			}
		}
		return newBoundSql;
	}

	public static class BoundSqlSqlSource implements SqlSource {
		BoundSql boundSql;

		public BoundSqlSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}

		@Override
		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}
}
