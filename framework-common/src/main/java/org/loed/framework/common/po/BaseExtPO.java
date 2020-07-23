package org.loed.framework.common.po;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Map;

/**
 * 扩展表的基类，包含了扩展属性信息
 *
 * @author Thomason
 * @version 1.0
 */
@MappedSuperclass
public abstract class BaseExtPO extends BasePO implements Serializable {
//	//扩展字段集合
//	protected Map<String, ?> extMap;
//
//	public Map<String, ?> getExtMap() {
//		return extMap;
//	}
//
//	public void setExtMap(Map<String, ?> extMap) {
//		this.extMap = extMap;
//	}
}
