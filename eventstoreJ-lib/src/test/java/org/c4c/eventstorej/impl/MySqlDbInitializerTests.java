package org.c4c.eventstorej.impl;

import org.c4c.eventstore.test.mysql.EmbeddedMySql;
import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.DbInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testable
public class MySqlDbInitializerTests extends EmbeddedMySql {

    @Test
    public void test_init_db_ok() throws Throwable {
        DbInitializer db = DbInitializers.getInstance(StoreType.MYSQL, conn, "event");
        Assertions.assertNotNull(db);
        db.initialize();
    }

    @Test()
    public void test_init_db_null_conn() {
        NullPointerException thrown = assertThrows(
                NullPointerException.class,
                () -> DbInitializers.getInstance(StoreType.MYSQL, null, "event"),
                "Expected doThing() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Connection"));
    }


    @Test()
    public void test_init_namespace_null() {
        NullPointerException thrown = assertThrows(
                NullPointerException.class,
                () -> DbInitializers.getInstance(StoreType.MYSQL, conn, null),
                "Expected doThing() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Namespace"));

    }

    @Test()
    public void test_init_namespace_empty() {
        NullPointerException thrown = assertThrows(
                NullPointerException.class,
                () -> DbInitializers.getInstance(StoreType.MYSQL, conn, ""),
                "Expected doThing() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Namespace"));
    }
}
