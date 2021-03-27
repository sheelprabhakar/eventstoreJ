package org.c4c.eventstore.test.mysql;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.MysqldConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.distribution.Version.v5_6_latest;

public abstract class EmbeddedMySql {
    // connecting to a running Postgres and feeding up the database
    public static Connection conn;
    private static  EmbeddedMysql mysqld;
    @BeforeAll
    public static void initiMySql() throws IOException, SQLException {
        MysqldConfig config = aMysqldConfig(v5_6_latest)
                .withPort(3307)
                .withUser("user", "")
                .build();

        mysqld = anEmbeddedMysql(config)
                .addSchema("eventstore")
                .start();
        conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3307/eventstore", "user", "");
    }

    @AfterAll
    public static void stopMySql() throws SQLException {
        // close db connection
        if(conn != null) {
            conn.close();
        }
        if(mysqld != null) {
            mysqld.stop();
        }
    }
}
