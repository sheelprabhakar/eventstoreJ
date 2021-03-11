package org.c4c.eventstorej.impl;

import org.c4c.eventstorej.Event;
import org.c4c.eventstorej.EventStoreJException;
import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.DbInitializer;
import org.c4c.eventstorej.api.EventStore;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventStoreJImpl implements EventStore {
    private final Connection connection;
    private final DbInitializer dbInitializer;
    private final String tableName;

    public EventStoreJImpl(final Connection connection, final DbInitializer dbInitializer, String namespace) {
        this.connection = connection;
        this.dbInitializer = dbInitializer;
        this.tableName = namespace;
    }

    @Override
    public void init() throws EventStoreJException {
        try {
            this.dbInitializer.initialize();
        } catch (IOException | SQLException e) {
            throw new EventStoreJException("Can not initialize store");
        }
    }

    public void saveEvent(Event event) throws SQLException {
        PreparedStatement stmt = this.connection.prepareStatement("");

    }
}
