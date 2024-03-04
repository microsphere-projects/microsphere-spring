package io.microsphere.spring.test.jdbc.embedded;

import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable an embedded database
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EmbeddedDataBaseBeanDefinitionRegistrar.class)
@Repeatable(EnableEmbeddedDatabases.class)
public @interface EnableEmbeddedDatabase {

    /**
     * @return Embedded Database Type, SQLite as default
     */
    EmbeddedDatabaseType type() default EmbeddedDatabaseType.SQLITE;

    /**
     * JDBC connection port
     *
     * @return if not specified port, return <code>-1</code>
     */
    int port() default -1;

    /**
     * @return {@link DataSource} Bean Name
     */
    String dataSource();

    /**
     * @return Primary Bean or not
     */
    boolean primary() default false;

    /**
     * @return JDBC Properties Properties, Key-value
     */
    String[] properties() default {};
}
