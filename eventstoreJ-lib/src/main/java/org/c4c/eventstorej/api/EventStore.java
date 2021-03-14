package org.c4c.eventstorej.api;

import org.c4c.eventstorej.Event;
import org.c4c.eventstorej.EventStoreJException;

import java.util.List;

public interface EventStore {

    void init() throws EventStoreJException, Throwable;

    int saveEvent(Event event) throws Throwable;


    int saveEvent(List<Event> eventList) throws Throwable;
}
