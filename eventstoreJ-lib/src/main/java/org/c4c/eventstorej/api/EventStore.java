package org.c4c.eventstorej.api;

import org.c4c.eventstorej.EventStoreJException;

public interface EventStore {

    void init() throws EventStoreJException, Throwable;
}
