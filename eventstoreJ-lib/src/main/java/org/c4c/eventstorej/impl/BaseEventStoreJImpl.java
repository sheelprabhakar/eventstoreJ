package org.c4c.eventstorej.impl;

import org.c4c.eventstorej.Event;
import org.c4c.eventstorej.EventStoreJException;
import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.DbInitializer;
import org.c4c.eventstorej.api.EventStore;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class BaseEventStoreJImpl implements EventStore {
    protected final Connection connection;
    protected final DbInitializer dbInitializer;
    protected final String eventsTableName;
    protected final String snapShotsTableName;

    public BaseEventStoreJImpl(final Connection connection, final DbInitializer dbInitializer, String namespace) {
        this.connection = connection;
        this.dbInitializer = dbInitializer;
        this.eventsTableName = namespace + "_events";
        this.snapShotsTableName = namespace + "_snapshots";
    }

    @Override
    public void init() throws Throwable {
        try {
            this.dbInitializer.initialize();
        } catch (IOException | SQLException e) {
            throw new EventStoreJException("Can not initialize store");
        }
    }

}
