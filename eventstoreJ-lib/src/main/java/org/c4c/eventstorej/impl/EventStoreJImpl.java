package org.c4c.eventstorej.impl;

import org.c4c.eventstorej.Event;
import org.c4c.eventstorej.EventStoreJException;
import org.c4c.eventstorej.Utils;
import org.c4c.eventstorej.api.DbInitializer;
import org.c4c.eventstorej.api.EventStore;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventStoreJImpl implements EventStore {
    private final Connection connection;
    private final DbInitializer dbInitializer;
    private final String eventsTableName;
    private final String snapShotsTableName;

    public EventStoreJImpl(final Connection connection, final DbInitializer dbInitializer, String namespace) {
        this.connection = connection;
        this.dbInitializer = dbInitializer;
        this.eventsTableName = namespace+"_events";
        this.snapShotsTableName = namespace+"_snapshots";
    }

    @Override
    public void init() throws Throwable {
        try {
            this.dbInitializer.initialize();
        } catch (IOException | SQLException e) {
            throw new EventStoreJException("Can not initialize store");
        }
    }

    @Override
    public boolean saveEvent(Event event) throws Throwable {
        PreparedStatement stmt = this.connection.prepareStatement("INSERT INTO "+ eventsTableName+
                " (aggregateId, revision, event, hasBeenPublished) values(?, ?, ? , ?)");
        prepareStatement(event, stmt);
        return stmt.execute();
    }

    private void prepareStatement(Event event, PreparedStatement stmt) throws Throwable {
        //Validate
        if(!Utils.isValidUUID(event.getAggregateId())){
            throw new IllegalArgumentException("Event has not valid UUID.");
        }
        if(event.getRevision() < 1){
            throw new IllegalArgumentException("Event has not valid revision.");
        }

        String eventData = event.getEventString();
        stmt.setString(1, event.getAggregateId());
        stmt.setInt(2, event.getRevision());
        stmt.setString(3, eventData);
        stmt.setBoolean(4, event.isHasBeenPublished());
    }
}
