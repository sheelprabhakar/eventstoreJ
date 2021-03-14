package org.c4c.eventstorej.impl;

import org.c4c.eventstorej.Event;
import org.c4c.eventstorej.EventStoreJException;
import org.c4c.eventstorej.Utils;
import org.c4c.eventstorej.api.DbInitializer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        PreparedStatement stmt = getPreparedInsertStatement();
        this.preparePGInsertStatement(event, stmt);

       if(stmt != null) {
           return stmt.executeUpdate();
       }else {
           return -1;
       }
    }

    private PreparedStatement getPreparedInsertStatement() throws SQLException {
        return this.connection.prepareStatement("INSERT INTO " + eventsTableName +
                " (aggregateId, revision, event, published) values(?, ?, to_json(?::json) , ?)");
    }

    @Override
    public <T> int saveEvent(List<Event<T>> eventList) throws Throwable {

        if(eventList == null || eventList.size() < 1){
                throw new IllegalArgumentException("Event list cannot be null or empty.");
        }
        PreparedStatement stmt = getPreparedInsertStatement();
        for (Event event: eventList) {
            this.preparePGInsertStatement(event, stmt);
            stmt.addBatch();
            stmt.clearParameters();
        }
        if(stmt != null) {
            int[] res = stmt.executeBatch();
            return res.length;
        }else {
            return -1;
        }
    }

    private void preparePGInsertStatement(Event event, PreparedStatement stmt) throws Throwable {

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
        stmt.setBoolean(4, event.isPublished());
    }

    @Override
    public <T> List<Event<T>> getEventList(String uuid,final Class<T> classType) throws Throwable{
    return getEventList(UUID.fromString(uuid), classType);
    }

    @Override
    public <T> List<Event<T>> getEventList(UUID uuid,  final Class<T> classType) throws Throwable{
        PreparedStatement stmt =this.connection.prepareStatement(
                "SELECT position, aggregateId, revision, event, published FROM "+ eventsTableName +
                " WHERE  aggregateId = ? ORDER BY revision DESC");
        stmt.setObject(1, uuid);
        ResultSet resultSet = stmt.executeQuery();
        List<Event<T>> eventList = new ArrayList<>();
        while(resultSet.next()){
            Event event = new Event();
            event.setPosition(resultSet.getBigDecimal(1).toBigInteger());
            event.setAggregateId(uuid);
            event.setRevision(resultSet.getInt(3));
            event.setEvent(Utils.convertJsonStringToObject(resultSet.getString(4), classType));
            event.setPublished(resultSet.getBoolean(5));
            eventList.add(event);
        }

        return eventList;
    }
}
