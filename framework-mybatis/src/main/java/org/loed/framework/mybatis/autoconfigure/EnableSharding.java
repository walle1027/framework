package org.loed.framework.mybatis.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/27 9:25 上午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ShardingAutoConfiguration.class})
public @interface EnableSharding {
}
