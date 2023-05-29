package io.microsphere.spring.test.jdbc.embedded;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Repeatable} {@link EnableEmbeddedDatabase}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EmbeddedDataBaseBeanDefinitionsRegistrar.class)
public @interface EnableEmbeddedDatabases {

    /**
     * @return 多个 {@link EnableEmbeddedDatabase} 配置
     */
    EnableEmbeddedDatabase[] value();
}
