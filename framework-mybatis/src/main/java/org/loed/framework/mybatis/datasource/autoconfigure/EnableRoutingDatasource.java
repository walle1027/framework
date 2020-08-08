package org.loed.framework.mybatis.datasource.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/3/1 上午9:24
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(RoutingDataSourceConfigure.class)
public @interface EnableRoutingDatasource {
}
