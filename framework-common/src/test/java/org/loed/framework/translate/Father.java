package org.loed.framework.translate;

import lombok.Data;
import org.loed.framework.common.po.CommonPO;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/28 14:47
 */
@Table(name = "t_father")
@Data
public class Father extends CommonPO {
	@Column
	private String fatherName;
	@Column
	private String calculateProp1;
	@OneToMany
	private List<Son> sons;

	@Override
	public String toString() {
		return "Father{" +
				"fatherName='" + fatherName + '\'' +
				", calculateProp1='" + calculateProp1 + '\'' +
				", sons=" + sons +
				'}';
	}

	public String getFatherName() {
		return fatherName;
	}

	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	public List<Son> getSons() {
		return sons;
	}

	public void setSons(List<Son> sons) {
		this.sons = sons;
	}

	public String getCalculateProp1() {
		return calculateProp1;
	}

	public void setCalculateProp1(String calculateProp1) {
		this.calculateProp1 = calculateProp1;
	}
}
