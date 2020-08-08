package org.loed.framework.r2dbc.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 9:31 上午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(R2dbcDaoRegister.class)
public @interface R2dbcDaoScanner {
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
