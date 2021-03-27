package org.c4c.eventstore.test.pg;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class EmbeddedPG {
    // connecting to a running Postgres and feeding up the database
    public static Connection conn;
    private static  EmbeddedPostgres pg;

    @BeforeAll
    public static void initiPG() throws IOException, SQLException {
        EmbeddedPostgres pg = EmbeddedPostgres.start();
        conn = pg.getPostgresDatabase().getConnection();
    }

    @AfterAll
    public static void stopPg() throws SQLException {
        // close db connection
        if(conn != null) {
            conn.close();
        }
        if(pg != null){
            try {
                pg.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
