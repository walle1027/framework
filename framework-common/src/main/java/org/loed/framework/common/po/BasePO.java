//package org.loed.framework.common.po;
//
//import lombok.Data;
//
//import javax.persistence.*;
//import java.io.Serializable;
//import java.util.Date;
//
///**
// * 所有的实体类的父类，集成了一些公用属性
// *
// * @author Thomason
// */
//@SuppressWarnings("serial")
//@MappedSuperclass
//@Data
//public abstract class BasePO extends CommonPO implements Serializable, Cloneable, Identify {
//	/**
//	 * 公司编号
//	 */
//	@Column(length = 50, updatable = false)
//	@TenantId
//	protected String tenantCode;
//}
