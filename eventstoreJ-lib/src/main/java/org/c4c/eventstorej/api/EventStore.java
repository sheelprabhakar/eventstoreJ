package org.c4c.eventstorej.api;

import org.c4c.eventstorej.Event;

import java.util.List;
import java.util.UUID;

public interface EventStore {

    void init() throws Throwable;

    int saveEvent(Event event) throws Throwable;

    <T> int saveEvent(List<Event<T>> eventList) throws Throwable;

    <T> List<Event<T>> getEventList(String aggregateId, final Class<T> classType) throws Throwable;

    <T> List<Event<T>> getEventList(UUID aggregateId, final Class<T> classType) throws Throwable;

    <T> List<Event<T>> getEventList(String aggregateId, int fromRevision, int toRevision, final Class<T> classType) throws Throwable;

    <T> List<Event<T>> getEventList(UUID aggregateId, int fromRevision, int toRevision, final Class<T> classType) throws Throwable;

    <T> Event<T> getLastEvent(String aggregateId, final Class<T> classType) throws Throwable;

    <T> Event<T> getLastEvent(UUID aggregateId, final Class<T> classType) throws Throwable;

    <T> List<Event<T>> getUnpublishedEventList(final Class<T> classType) throws Throwable;

    int updateToPublished(String aggregateId, int fromRevision, int toRevision) throws Throwable;

    <T> int updateToPublished(UUID aggregateId, int fromRevision, int toRevision) throws Throwable;

    int saveSnapShot(String aggregateId, int revision, Object state) throws Throwable;

    int saveSnapShot(UUID aggregateId, int revision, Object state) throws Throwable;

    <T> T getSnapShot(String aggregateId, final Class<T> classType) throws Throwable;

    <T> T getSnapShot(UUID aggregateId, final Class<T> classType) throws Throwable;

    <T> List<Event<T>> getReplay(int fromPosition, int toPosition, final Class<T> classType) throws Throwable;
}
