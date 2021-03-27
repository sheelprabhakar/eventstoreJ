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

public class MySqlEventStoreJImpl extends BaseEventStoreJImpl {

    public MySqlEventStoreJImpl(final Connection connection, final DbInitializer dbInitializer, String namespace) {
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

    /**
     * Method to save event in database
     * @param event object, to be stored in db
     * @return count of saved item
     * @throws Throwable
     */
    @Override
    public int saveEvent(Event event) throws Throwable {
        PreparedStatement stmt = getPreparedInsertStatement();
        this.prepareMySqlInsertStatement(event, stmt);

        if (stmt != null) {
            return stmt.executeUpdate();
        } else {
            return -1;
        }
    }

    private PreparedStatement getPreparedInsertStatement() throws SQLException {
        return this.connection.prepareStatement("INSERT INTO " + eventsTableName +
                " (aggregateId, revision, event, published) values(?, ?, ? , ?)");
    }

    @Override
    public <T> int saveEvent(List<Event<T>> eventList) throws Throwable {

        if (eventList == null || eventList.size() < 1) {
            throw new IllegalArgumentException("Event list cannot be null or empty.");
        }
        PreparedStatement stmt = getPreparedInsertStatement();
        for (Event event : eventList) {
            this.prepareMySqlInsertStatement(event, stmt);
            stmt.addBatch();
            stmt.clearParameters();
        }
        if (stmt != null) {
            int[] res = stmt.executeBatch();
            return res.length;
        } else {
            return -1;
        }
    }

    private void prepareMySqlInsertStatement(Event event, PreparedStatement stmt) throws Throwable {

        //Validate
        if (event.getAggregateId() == null) {
            throw new IllegalArgumentException("Event has invalid UUID.");
        }
        if (event.getRevision() < 1) {
            throw new IllegalArgumentException("Event has invalid revision.");
        }

        if (event.getEvent() == null) {
            throw new IllegalArgumentException("Event has invalid data.");
        }

        String eventData = event.getEventString();
        stmt.setString(1, event.getAggregateId().toString());
        stmt.setInt(2, event.getRevision());
        stmt.setString(3, eventData);
        stmt.setBoolean(4, event.isPublished());
    }

    @Override
    public <T> List<Event<T>> getEventList(String aggregateId, final Class<T> classType) throws Throwable {
        if (!Utils.isValidUUID(aggregateId)) {
            throw new IllegalArgumentException("Event has invalid aggregateId.");
        }
        return getEventList(UUID.fromString(aggregateId), classType);
    }

    @Override
    public <T> List<Event<T>> getEventList(UUID aggregateId, final Class<T> classType) throws Throwable {
        PreparedStatement stmt = this.connection.prepareStatement(
                "SELECT position, aggregateId, revision, event, published FROM " + eventsTableName +
                        " WHERE  aggregateId = ? ORDER BY revision DESC");
        stmt.setString(1, aggregateId.toString());
        ResultSet resultSet = stmt.executeQuery();
        return getEventListFromResultSet(resultSet, classType);

    }

    private <T> List<Event<T>> getEventListFromResultSet(ResultSet resultSet, Class<T> classType) throws SQLException, IOException {
        List<Event<T>> eventList = new ArrayList<>();
        while (resultSet.next()) {
            eventList.add(getEventFromResultSet(resultSet, classType));
        }
        return eventList;
    }

    private <T> Event<T> getEventFromResultSet(ResultSet resultSet, Class<T> classType) throws SQLException, IOException {
        Event<T> event = new Event<T>();
        event.setPosition(resultSet.getBigDecimal(1).toBigInteger());
        event.setAggregateId(UUID.fromString(resultSet.getString(2)));
        event.setRevision(resultSet.getInt(3));
        event.setEvent(Utils.convertJsonStringToObject(resultSet.getString(4), classType));
        event.setPublished(resultSet.getBoolean(5));
        return event;
    }

    @Override
    public <T> List<Event<T>> getEventList(String aggregateId, int fromRevision, int toRevision, final Class<T> classType) throws Throwable {
        if (!Utils.isValidUUID(aggregateId)) {
            throw new IllegalArgumentException("Event has invalid aggregateId.");
        }
        return getEventList(UUID.fromString(aggregateId), fromRevision, toRevision, classType);
    }

    @Override
    public <T> List<Event<T>> getEventList(UUID aggregateId, int fromRevision, int toRevision, final Class<T> classType) throws Throwable {
        //Validate
        if (fromRevision > toRevision) {
            throw new IllegalArgumentException("FromRevision can not be greater than toRevision.");
        }
        PreparedStatement stmt = this.connection.prepareStatement(
                "SELECT position, aggregateId, revision, event, published FROM " + eventsTableName +
                        " WHERE  aggregateId = ? AND revision BETWEEN ? and ? ORDER BY revision DESC");
        stmt.setString(1, aggregateId.toString());
        stmt.setInt(2, fromRevision);
        stmt.setInt(3, toRevision);
        ResultSet resultSet = stmt.executeQuery();
        return getEventListFromResultSet(resultSet, classType);
    }

    @Override
    public <T> Event<T> getLastEvent(String aggregateId, final Class<T> classType) throws Throwable {
        if (!Utils.isValidUUID(aggregateId)) {
            throw new IllegalArgumentException("Event has invalid aggregateId.");
        }
        return getLastEvent(UUID.fromString(aggregateId), classType);
    }

    @Override
    public <T> Event<T> getLastEvent(UUID aggregateId, final Class<T> classType) throws Throwable {
        PreparedStatement stmt = this.connection.prepareStatement(
                "SELECT position, aggregateId, revision, event, published FROM " + eventsTableName +
                        " WHERE aggregateId = ? AND revision =(SELECT MAX(revision) FROM " + eventsTableName+" WHERE aggregateId = ? )");
        stmt.setString(1, aggregateId.toString());
        stmt.setString(2, aggregateId.toString());

        ResultSet resultSet = stmt.executeQuery();
        if(resultSet.next()) {
            return getEventFromResultSet(resultSet, classType);
        }else{
            return null;
        }
    }

    @Override
    public <T> List<Event<T>> getUnpublishedEventList(final Class<T> classType) throws Throwable {

        PreparedStatement stmt = this.connection.prepareStatement(
                "SELECT position, aggregateId, revision, event, published FROM " + eventsTableName +
                        " WHERE published = false ORDER BY position ASC");
        ResultSet resultSet = stmt.executeQuery();
        return getEventListFromResultSet(resultSet, classType);
    }

    @Override
    public int updateToPublished(String aggregateId, int fromRevision, int toRevision) throws Throwable {
        if (!Utils.isValidUUID(aggregateId)) {
            throw new IllegalArgumentException("Event has invalid aggregateId.");
        }
        return updateToPublished(UUID.fromString(aggregateId), fromRevision, toRevision);
    }
    @Override
    public int updateToPublished(UUID aggregateId, int fromRevision, int toRevision) throws Throwable {
        //Validate
        if (fromRevision > toRevision) {
            throw new IllegalArgumentException("FromRevision can not be greater than toRevision.");
        }
        PreparedStatement stmt = this.connection.prepareStatement(
                "UPDATE  " + eventsTableName +
                        " SET published = true WHERE  aggregateId = ? AND revision BETWEEN ? and ? ");
        stmt.setString(1, aggregateId.toString());
        stmt.setInt(2, fromRevision);
        stmt.setInt(3, toRevision);
        return stmt.executeUpdate();
    }

    @Override
    public int saveSnapShot(String aggregateId, int revision, Object state) throws Throwable{
        if (!Utils.isValidUUID(aggregateId)) {
            throw new IllegalArgumentException("Event has invalid aggregateId.");
        }
        return saveSnapShot(UUID.fromString(aggregateId), revision, state);
    }

    @Override
    public int saveSnapShot(UUID aggregateId, int revision, Object state) throws Throwable{
        PreparedStatement stmt = this.getPreparedSnapShotInsertStatement();
        stmt.setString(1, aggregateId.toString());
        stmt.setInt(2, revision);
        stmt.setString(3,   Utils.convertObjectToJsonString(state));

        return stmt.executeUpdate();
    }

    private PreparedStatement getPreparedSnapShotInsertStatement() throws SQLException {
        return this.connection.prepareStatement("INSERT INTO " + snapShotsTableName +
                " (aggregateId, revision, state) values (?, ?, ?)");
    }

    @Override
    public <T> T getSnapShot(String aggregateId, final Class<T> classType) throws Throwable {
        //Validate
        if (!Utils.isValidUUID(aggregateId)) {
            throw new IllegalArgumentException("Event has invalid aggregateId.");
        }
        return getSnapShot(UUID.fromString(aggregateId), classType);
    }
    @Override
    public <T> T getSnapShot(UUID aggregateId, final Class<T> classType) throws Throwable {
        //Validate
        if (aggregateId == null) {
            throw new IllegalArgumentException("Event has invalid aggregateId.");
        }
        PreparedStatement stmt = this.connection.prepareStatement(
                "SELECT aggregateId, revision, state FROM " + snapShotsTableName +
                        " WHERE aggregateId = ? AND revision =(SELECT MAX(revision) FROM " + snapShotsTableName+" WHERE aggregateId = ? )");
        stmt.setString(1, aggregateId.toString());
        stmt.setString(2, aggregateId.toString());

        ResultSet resultSet = stmt.executeQuery();
        if(resultSet.next()) {
            return Utils.convertJsonStringToObject(resultSet.getString(3), classType);
        }else{
            return null;
        }
    }

    @Override
    public <T> List<Event<T>> getReplay(int fromPosition, int toPosition, final Class<T> classType) throws Throwable {
        //Validate
        if (fromPosition > toPosition) {
            throw new IllegalArgumentException("fromPosition can not be greater than toPosition.");
        }
        PreparedStatement stmt = this.connection.prepareStatement(
                "SELECT position, aggregateId, revision, event, published  FROM " + eventsTableName +
                        " WHERE position BETWEEN ? and ? ");
        stmt.setInt(1, fromPosition);
        stmt.setInt(2, toPosition);
        ResultSet resultSet = stmt.executeQuery();
        return getEventListFromResultSet(resultSet, classType);
    }
}
