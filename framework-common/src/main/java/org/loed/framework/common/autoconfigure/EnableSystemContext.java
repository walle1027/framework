package org.loed.framework.common.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/7 1:16 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(SystemContextFilterAutoConfiguration.class)
public @interface EnableSystemContext {
}
