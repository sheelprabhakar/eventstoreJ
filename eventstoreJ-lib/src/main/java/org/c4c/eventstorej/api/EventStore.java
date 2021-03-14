package org.c4c.eventstorej.api;

import org.c4c.eventstorej.Event;
import org.c4c.eventstorej.EventStoreJException;

import java.util.List;
import java.util.UUID;

public interface EventStore {

    void init() throws EventStoreJException, Throwable;

    int saveEvent(Event event) throws Throwable;

    <T> int saveEvent(List<Event<T>> eventList) throws Throwable;

    <T> List<Event<T>> getEventList(String uuid, final Class<T> classType) throws Throwable;

    <T> List<Event<T>> getEventList(UUID uuid, final Class<T> classType) throws Throwable;

    <T> List<Event<T>> getEventList(String uuid, int fromRevision, int toRevision, final Class<T> classType) throws Throwable;

    <T> List<Event<T>> getEventList(UUID uuid, int fromRevision, int toRevision, final Class<T> classType) throws Throwable;

    <T> Event<T> getLastEvent(String uuid, final Class<T> classType) throws Throwable;

    <T> Event<T> getLastEvent(UUID uuid, final Class<T> classType) throws Throwable;

    <T> List<Event<T>> getUnpublishedEventList(final Class<T> classType) throws Throwable;

    int updateToPublished(String uuid, int fromRevision, int toRevision) throws Throwable;

    <T> int updateToPublished(UUID uuid, int fromRevision, int toRevision) throws Throwable;
}
