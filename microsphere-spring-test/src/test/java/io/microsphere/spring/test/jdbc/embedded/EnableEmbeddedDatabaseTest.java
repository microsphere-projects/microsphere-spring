package io.microsphere.spring.test.jdbc.embedded;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static io.microsphere.spring.test.jdbc.embedded.EmbeddedDatabaseType.MARIADB;
import static org.junit.Assert.assertTrue;

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
@EnableEmbeddedDatabase(
        type = MARIADB, port = 3306, dataSource = "mariadbDataSource",
        properties = {
                "charset=utf-8",
                "useSSL=false",
                "useUnicode=true",
                "_sql.source=classpath:/META-INF/mysql/manual_schema.sql"
        }
)
public class EnableEmbeddedDatabaseTest {

    private static final Logger logger = LoggerFactory.getLogger(EnableEmbeddedDatabaseTest.class);

    @Autowired
    @Qualifier("primary")
    private DataSource dataSource;

    @Autowired
    @Qualifier("secondary")
    private DataSource dataSource2;

    @Autowired
    @Qualifier("mariadbDataSource")
    private DataSource mariadbDataSource;

    @Test
    public void test() throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.close();
        connection.close();
    }

    @Test
    public void testMariadbDataSource() throws Exception {
        Connection connection = mariadbDataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SHOW databases");
        while (resultSet.next()) {
            String databaseName = resultSet.getString(1);
            logger.info("database : {}", databaseName);
        }
        statement.close();
        connection.close();
    }
}

