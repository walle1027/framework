package org.loed.framework.common.query;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.lambda.SFunction;

import javax.persistence.criteria.JoinType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 表单上的查询条件封装类
 * 封装表单的查询条件
 *
 * @author Thomason
 * Date: 11-4-22
 * Time: 下午2:16
 * @version 1.0
 */
@ToString
public class Condition implements Serializable {
	/**
	 * 别名分隔符
	 */
	public static final String ALIAS_SEPARATOR = "_";
	/**
	 * 路径分隔符
	 */
	public static final String PATH_SEPARATOR = ".";
	/**
	 * 条件连接字符串 and or
	 */
	private Joint joint = Joint.and;
	/**
	 * 实体对应的属性名称
	 */
	private String propertyName;
	/**
	 * 多个属性之间的连接方式
	 */
	private JoinType joinType = JoinType.INNER;
	/**
	 * 查询关系符号
	 */
	private Operator operator = Operator.equal;
	/**
	 * 条件的值
	 */
	private Object value;
	/**
	 * 子条件
	 */
	private List<Condition> subConditions;

	public Condition() {
	}

	public Condition(String propertyName, String value) {
		this.propertyName = propertyName;
		this.value = value;
	}

	public Condition(String propertyName, Operator operator, Object value) {
		this.propertyName = propertyName;
		this.operator = operator;
		this.value = value;
	}

	public <T> Condition(SFunction<T, ?> lambda, Operator operator, Object value) {
		this.propertyName = LambdaUtils.getPropFromLambda(lambda);
		this.operator = operator;
		this.value = value;
	}

	public boolean hasSubCondition() {
		return this.subConditions != null && this.subConditions.size() > 0;
	}

	/**
	 * 判断属性是否为关联属性
	 *
	 * @return
	 */
	@JsonIgnore
	public boolean isRelativeProperty() {
		if (propertyName != null && !propertyName.isEmpty()) {
			if (propertyName.contains(PATH_SEPARATOR)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 取得路径组
	 * 如果属性是关联属性，
	 * 那么这个将返回关联属性自动生成的别名
	 * 别名声称规则为：将属性中的点号换成下划线
	 * eg a.b  ---> a_b
	 *
	 * @return 路径组
	 */
	@JsonIgnore
	public List<AssociationPath> getPathList() {
		if (propertyName.contains(PATH_SEPARATOR)) {
			List<AssociationPath> pathList = new ArrayList<AssociationPath>();
			StringBuilder builder = new StringBuilder();
			String s = propertyName;
			while (s.contains(PATH_SEPARATOR)) {
				if (builder.length() > 0) {
					builder.append(PATH_SEPARATOR);
				}
				int i = s.indexOf(PATH_SEPARATOR);
				builder.append(s.substring(0, i));
				String path = builder.toString();
				String alias = path.replace(PATH_SEPARATOR, ALIAS_SEPARATOR);
				AssociationPath associationPath = new AssociationPath(path, alias);
				pathList.add(associationPath);
				//进入下一次循环
				s = s.substring(i + 1);
			}
			return pathList;
		}
		return null;
	}

	/**
	 * 取得在criteria中真实的属性名称
	 * 如果为简单属性，直接返回属性名
	 * 如果为复杂属性，返回复杂属性的别名+属性名
	 *
	 * @return
	 */
	@JsonIgnore
	public String getRealPropertyName() {
		if (isRelativeProperty()) {
			String path = propertyName.substring(0, propertyName.lastIndexOf(PATH_SEPARATOR));
			String prop = propertyName.substring(propertyName.lastIndexOf(PATH_SEPARATOR));
			String alias = "";
			List<AssociationPath> pathList = getPathList();
			if (CollectionUtils.isNotEmpty(pathList)) {
				for (AssociationPath associationPath : pathList) {
					if (path.equals(associationPath.getPath())) {
						alias = associationPath.getAlias();
						break;
					}
				}
			}
			return alias + prop;
		}
		return propertyName;
	}

	public Joint getJoint() {
		return joint;
	}

	public void setJoint(Joint joint) {
		this.joint = joint;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public List<Condition> getSubConditions() {
		return subConditions;
	}

	public void setSubConditions(List<Condition> subConditions) {
		this.subConditions = subConditions;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
}
