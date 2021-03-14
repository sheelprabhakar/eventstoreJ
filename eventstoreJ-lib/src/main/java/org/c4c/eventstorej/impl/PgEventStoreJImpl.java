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

public class PgEventStoreJImpl extends BaseEventStoreJImpl {

    public PgEventStoreJImpl(final Connection connection, final DbInitializer dbInitializer, String namespace) {
        super(connection, dbInitializer, namespace);
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
    public int saveEvent(Event event) throws Throwable {
        PreparedStatement stmt = this.preparePGInsertStatement(event);

       if(stmt != null) {
           return stmt.executeUpdate();
       }else {
           return -1;
       }
    }

    private PreparedStatement preparePGInsertStatement(Event event) throws Throwable {
        PreparedStatement stmt = this.connection.prepareStatement("INSERT INTO " + eventsTableName +
                " (aggregateId, revision, event, hasBeenPublished) values(?, ?, to_json(?::json) , ?)");
        //Validate
        if (event.getAggregateId() == null) {
            throw new IllegalArgumentException("Event has invalid UUID.");
        }
        if (event.getRevision() < 1) {
            throw new IllegalArgumentException("Event has invalid revision.");
        }

        if (event.getEvent()== null) {
            throw new IllegalArgumentException("Event has invalid data.");
        }

        String eventData = event.getEventString();
        stmt.setObject(1, event.getAggregateId());
        stmt.setInt(2, event.getRevision());
        stmt.setString(3, eventData);
        stmt.setBoolean(4, event.isHasBeenPublished());

        return stmt;
    }
}
