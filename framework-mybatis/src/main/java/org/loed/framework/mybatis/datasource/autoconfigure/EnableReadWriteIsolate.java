package org.loed.framework.mybatis.datasource.autoconfigure;

import org.loed.framework.mybatis.inspector.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/6/20 2:39 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ReadWriteAopAutoConfiguration.class, ReadWriteDataSourceConfiguration.class})
@EnableConfigurationProperties(DataSourceProperties.class)
public @interface EnableReadWriteIsolate {
}
