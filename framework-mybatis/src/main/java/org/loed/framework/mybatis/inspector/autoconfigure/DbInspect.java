package org.loed.framework.mybatis.inspector.autoconfigure;

import org.loed.framework.mybatis.inspector.dialect.Dialect;
import org.loed.framework.mybatis.inspector.dialect.impl.MysqlDialect;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.lang.annotation.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/5/26 下午11:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DbInspectorBeanRegister.class)
@AutoConfigureAfter(DataSource.class)
public @interface DbInspect {
	Class<? extends Dialect> dialect() default MysqlDialect.class;

	/**
	 * 是否自动启用
	 */
	boolean enabled() default true;

	/**
	 * Alias for the {@link #basePackages()} attribute. Allows for more concise
	 * annotation declarations e.g.:
	 * {@code @EnableMyBatisMapperScanner("org.my.pkg")} instead of {@code
	 *
	 * @EnableMyBatisMapperScanner(basePackages= "org.my.pkg"})}.
	 */
	String[] value() default {};

	/**
	 * Base packages to scan for MyBatis interfaces. Note that only interfaces
	 * with at least one method will be registered; concrete classes will be
	 * ignored.
	 */
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages
	 * to scan for annotated components. The package of each class specified will be scanned.
	 * <p>Consider creating a special no-op marker class or interface in each package
	 * that serves no purpose other than being referenced by this attribute.
	 */
	Class<?>[] basePackageClasses() default {};
}
