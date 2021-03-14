package org.c4c.eventstorej.api;

import java.io.IOException;
import java.sql.SQLException;

public interface DbInitializer {
    void initialize() throws IOException, SQLException, Throwable;
}
