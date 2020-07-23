package org.loed.framework.jdbc;

import org.loed.framework.common.ORMapping;
import org.loed.framework.common.query.Pagination;
import org.loed.framework.common.SqlBuilder;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.po.BasePO;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.util.ReflectionUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.loed.framework.common.SqlBuilder.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/10/7 下午3:29
 */
@SuppressWarnings({"unchecked", "NullableProblems", "UnusedDeclaration", "SimplifiableIfStatement"})
public class BaseJdbcDao<T extends BasePO> extends NamedParameterJdbcDaoSupport implements BaseDao<T> {
	private String tenantCodeColumn = "tenant_code";
	private Table table;

	public BaseJdbcDao() {
		super();
		Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
		if (entityClass == null) {
			throw new IllegalJpaClassException();
		}
		table = ORMapping.get(entityClass);
		if (table == null) {
			throw new IllegalJpaClassException();
		}
	}

	@Override
	public int insert(T entity) {
		setInsertDefaultValue(entity);
		SqlBuilder builder = new SqlBuilder();
		builder.append("insert into").append(table.getSqlName()).append("(");
		table.getColumns().forEach(column -> {
			builder.append(table.getSqlName()).append(",");
		});
		builder.deleteLastChar();
		builder.append(")");
		builder.append("values (");
		table.getColumns().forEach(column -> {
			builder.append("?").append(",");
			builder.parameter(ReflectionUtils.getFieldValue(entity, column.getJavaName()));
		});
		builder.deleteLastChar().append(")");
		return getNamedParameterJdbcTemplate().update(builder.toString(), builder.getParameters());
	}

	private void setInsertDefaultValue(T entity) {
//		if (StringUtils.isBlank(entity.getId())) {
//			entity.setId(UUIDUtils.getUUID());
//		}
		entity.setCreateTime(new Date());
		entity.setUpdateTime(new Date());
		entity.setCreateBy(SystemContext.getAccountId());
		entity.setUpdateBy(SystemContext.getAccountId());
		entity.setTenantCode(SystemContext.getTenantCode());
	}

	private void setUpdateDefaultValue(T entity) {
		entity.setUpdateTime(new Date());
		entity.setUpdateBy(SystemContext.getAccountId());
		entity.setTenantCode(SystemContext.getTenantCode());
	}

	@Override
	public int batchInsert(List<T> entityList) {
		//set default value
		for (T t : entityList) {
			setInsertDefaultValue(t);
		}
		SqlBuilder builder = new SqlBuilder();
		builder.append("insert into").append(table.getSqlName()).append("(");
		table.getColumns().forEach(column -> {
			builder.append(column.getSqlName()).append(",");
		});
		builder.deleteLastChar();
		builder.append(") values (");
		table.getColumns().forEach(column -> {
			builder.append(":" + column.getJavaName()).append(",");
		});
		builder.deleteLastChar();
		Map<String, Object>[] batchParams = new Map[entityList.size()];
		for (int i = 0; i < entityList.size(); i++) {
			T t = entityList.get(i);
			Map<String, Object> paramMap = new HashMap<>();
			table.getColumns().parallelStream().forEach(column -> paramMap.put(column.getJavaName(), ReflectionUtils.getFieldValue(t, column.getJavaName())));
			batchParams[i] = paramMap;
		}

		int[] ints = getNamedParameterJdbcTemplate().batchUpdate(builder.toString(), batchParams);
		return ints.length;
//		return getNamedParameterJdbcTemplate().update(builder.toString(), builder.getParameters());
	}

	@Override
	public void update(T entity) {
		setUpdateDefaultValue(entity);
		SqlBuilder builder = new SqlBuilder();
		builder.append("update").append(table.getSqlName()).append("set");
		table.getColumns().forEach(column -> {
			builder.append(column.getSqlName()).append("=").append("?");
			builder.parameter(ReflectionUtils.getFieldValue(entity, column.getJavaName()));
			builder.append(",");
		});
		builder.deleteLastChar();
		builder.append("where id = ?");
		builder.parameter(entity.getId());
		builder.append("and").append(tenantCodeColumn).append("=").append("?");
		builder.parameter(SystemContext.getTenantCode());
		getNamedParameterJdbcTemplate().update(builder.toString(), builder.getParameters());
	}

	@Override
	public void updateSelective(T entity) {
		setUpdateDefaultValue(entity);
		SqlBuilder builder = new SqlBuilder();
		builder.append("update").append(table.getSqlName()).append("set");
		table.getColumns().forEach(column -> {
			Object fieldValue = ReflectionUtils.getFieldValue(entity, column.getJavaName());
			boolean updatable = true;
			if (fieldValue == null) {
				updatable = false;
			}
			//TODO 空字符串?
			/*else	if(fieldValue instanceof String && StringUtils.isBlank((String)fieldValue)){
				updatable = false;
			}*/
			if (updatable) {
				builder.append(column.getSqlName()).append("=").append("?");
				builder.parameter(ReflectionUtils.getFieldValue(entity, column.getJavaName()));
				builder.append(",");
			}
		});
		builder.deleteLastChar();
		builder.append("where id = ?");
		builder.parameter(entity.getId());
		builder.append(AND).append(tenantCodeColumn).append("=").append("?");
		builder.parameter(SystemContext.getTenantCode());
		getNamedParameterJdbcTemplate().update(builder.toString(), builder.getParameters());
	}

	@Override
	public int delete(String id) {
		SqlBuilder builder = new SqlBuilder();
		builder.append(DELETE).append(FROM).append(table.getSqlName()).append(WHERE);
		builder.append("id = ?").parameter(id);
		builder.append(AND).append(tenantCodeColumn).append("= ?").parameter(SystemContext.getTenantCode());
		return getNamedParameterJdbcTemplate().update(builder.toString(), builder.getParameters());
	}

	@Override
	public T get(String id) {
		return null;
	}

	@Override
	public List<T> findByProperty(String propName, Object propValue) {
		return null;
	}

	@Override
	public List<T> findByCriteria(Criteria criteria) {
		return null;
	}

	@Override
	public Pagination<T> findPageByProperty(Pagination<T> pagination, String propName, Object propValue) {
		return null;
	}

	@Override
	public Pagination<T> findPageByCriteria(Pagination<T> pagination, Criteria criteria) {
		return null;
	}
}
