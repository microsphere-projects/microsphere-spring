package io.microsphere.spring.test.jdbc.embedded;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * {@link EnableEmbeddedDatabase} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EnableEmbeddedDatabaseTest.class)
@EnableEmbeddedDatabase(dataSource = "primary", primary = true)
@EnableEmbeddedDatabase(dataSource = "secondary")
public class EnableEmbeddedDatabaseTest {

    @Autowired
    @Qualifier("primary")
    private DataSource dataSource;

    @Autowired
    @Qualifier("secondary")
    private DataSource dataSource2;

    @Test
    public void test() throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.close();
        connection.close();
    }

}

