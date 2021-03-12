package org.c4c.eventstorej.impl;

import org.c4c.eventstore.test.pg.EmbeddedPG;
import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.DbInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
public class PgDbInitializerTests extends EmbeddedPG {

    @Test
    public void test_initdb_ok(){
        DbInitializer db= DbInitializers.getInstance(StoreType.POSTGRES, this.conn, "event");
        Assertions.assertNotNull(db);
    }
}
