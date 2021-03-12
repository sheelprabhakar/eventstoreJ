package org.c4c.eventstore.test.pg;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

public abstract class EmbeddedPG {


    // connecting to a running Postgres and feeding up the database
    public static Connection conn;

    @BeforeAll
    public static void initiPG() throws IOException, SQLException {
        EmbeddedPostgres pg = EmbeddedPostgres.start();
        conn = pg.getPostgresDatabase().getConnection();
    }

    @AfterAll
    public static void stopPg() throws SQLException {
        // close db connection
        conn.close();
    }

}
