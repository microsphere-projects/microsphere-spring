package io.microsphere.spring.jdbc.p6spy.beans.factory.config;

import com.p6spy.engine.spy.P6DataSource;
import io.microsphere.logging.test.jupiter.LoggingLevelsClass;
import io.microsphere.spring.test.jdbc.embedded.EnableEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link P6DataSourceBeanPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {P6DataSourceBeanPostProcessor.class, P6DataSourceBeanPostProcessorTest.class})
@TestPropertySource(properties = {"microsphere.jdbc.p6spy.excluded-datasource-beans=nonWrappedDataSource"})
@EnableEmbeddedDatabase(dataSource = "nonWrappedDataSource")
@EnableEmbeddedDatabase(dataSource = "wrappedDataSource")
@LoggingLevelsClass(levels = {"TRACE", "INFO", "ERROR"})
class P6DataSourceBeanPostProcessorTest {

    @Autowired
    @Qualifier("nonWrappedDataSource")
    private DataSource nonWrappedDataSource;

    @Autowired
    @Qualifier("wrappedDataSource")
    private DataSource wrappedDataSource;

    @Test
    void test() throws Exception {
        assertEquals(DriverManagerDataSource.class, nonWrappedDataSource.getClass());
        assertEquals(P6DataSource.class, wrappedDataSource.getClass());
        assertEquals(DriverManagerDataSource.class, wrappedDataSource.unwrap(DataSource.class).getClass());
    }
}
