package io.microsphere.spring.jdbc.p6spy.annotation;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.logging.LoggingEventListener;
import io.microsphere.logging.test.jupiter.LoggingLevelsClass;
import io.microsphere.spring.jdbc.p6spy.beans.factory.config.P6DataSourceBeanPostProcessor;
import io.microsphere.spring.test.jdbc.embedded.EnableEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link EnableP6DataSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        EnableP6DataSourceTest.class
})
@EnableP6DataSource
@EnableEmbeddedDatabase(dataSource = "testDataSource")
@LoggingLevelsClass(
        loggingClasses = P6DataSourceBeanPostProcessor.class,
        levels = {"TRACE", "INFO", "ERROR"}
)
class EnableP6DataSourceTest extends LoggingEventListener {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @Test
    void test() throws Exception {
        Connection connection = dataSource.getConnection();
        assertNotNull(connection);
    }

    @Override
    public void onBeforeGetConnection(ConnectionInformation connectionInformation) {
        try {
            assertSame(dataSource.unwrap(DataSource.class), connectionInformation.getDataSource());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
