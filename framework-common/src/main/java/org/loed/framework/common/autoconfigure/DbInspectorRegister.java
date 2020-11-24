package org.loed.framework.common.autoconfigure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/11/24 12:05 下午
 */
public class DbInspectorRegister {
	/**
	 * 注册扫描的包
	 */
	private static List<String> packages = new ArrayList<>();
	/**
	 * 注册扫描的类
	 */
	private static List<String> classes = new ArrayList<>();

	private DbInspectorRegister() {
	}

	public static void addPackages(String... packages) {
		if (packages != null && packages.length > 0) {
			Collections.addAll(DbInspectorRegister.packages, packages);
		}
	}

	public static void addPackages(Class<?>... basePackageClasses) {
		if (basePackageClasses != null && basePackageClasses.length > 0) {
			for (Class<?> basePackageClass : basePackageClasses) {
				DbInspectorRegister.packages.add(basePackageClass.getPackage().getName());
			}
		}
	}

	public static void addClasses(String... classes) {
		if (classes != null && classes.length > 0) {
			Collections.addAll(DbInspectorRegister.classes, classes);
		}
	}

	public static void addClasses(Class<?>... classes) {
		if (classes != null && classes.length > 0) {
			for (Class<?> cls : classes) {
				DbInspectorRegister.classes.add(cls.getName());
			}
		}
	}


	public static List<String> getPackages() {
		return packages;
	}

	public static List<String> getClasses() {
		return classes;
	}
}
