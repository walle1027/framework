package org.loed.framework.common.orm;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/6/4 下午11:49
 */
public class JoinColumn {
	private String name;

	private String referencedColumnName = "id";

	public JoinColumn(String name) {
		this.name = name;
	}

	public JoinColumn(String name, String referencedColumnName) {
		this.name = name;
		this.referencedColumnName = referencedColumnName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReferencedColumnName() {
		return referencedColumnName;
	}

	public void setReferencedColumnName(String referencedColumnName) {
		this.referencedColumnName = referencedColumnName;
	}
}
