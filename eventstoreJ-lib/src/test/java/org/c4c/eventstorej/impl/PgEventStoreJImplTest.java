package org.c4c.eventstorej.impl;

import org.c4c.eventstore.test.pg.EmbeddedPG;
import org.c4c.eventstorej.Event;
import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.EventStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testable
public class PgEventStoreJImplTest extends EmbeddedPG {
    static EventStore eventStore;
    private final static String NAME_SPACE = "event";

    @BeforeAll
    static void initEventStore() {
        eventStore = new PgEventStoreJImpl(conn, DbInitializers.getInstance(StoreType.POSTGRES, conn, NAME_SPACE), NAME_SPACE);
        try {
            eventStore.init();
        } catch (Throwable throwable) {
            Assertions.assertTrue(false);
        }
    }

    @Test()
    @Order(1)
    public void init_ok() {
        try {
            eventStore.init();
        } catch (Throwable throwable) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    @Order(2)
    public void save_event_ok() throws Throwable {
        Event<Person> event = getPersonEvent("Sheel", 40);

        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
    }

    @Test
    @Order(3)
    public void save_event_invalid_params() throws Throwable {
        Event<Person> event = new Event<>();
        event.setAggregateId(null);
        Person person = new Person("Sheel", 40);
        event.setEvent(person);
        event.setRevision(1);
        event.setPublished(false);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.saveEvent(event),
                "Expected saveEvent() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("invalid UUID"));
        event.setAggregateId(UUID.randomUUID());
        event.setEvent(null);

        thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.saveEvent(event),
                "Expected saveEvent() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("invalid data"));

        event.setEvent(person);
        event.setRevision(0);
        thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.saveEvent(event),
                "Expected saveEvent() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("invalid revision"));

        event.setRevision(-1);
        thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.saveEvent(event),
                "Expected saveEvent() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("invalid revision"));
    }

    @Test
    @Order(4)
    public void save_eventList_ok() throws Throwable {
        List<Event<Person>> eventList = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            Event<Person> event = getPersonEvent("Sindhu" + i, 40);
            eventList.add(event);
        }

        Assertions.assertTrue(eventStore.saveEvent(eventList) == 5);
    }

    @Test
    @Order(5)
    public void save_eventList_invalid_param() throws Throwable {
        final List<Event<Person>> eventList = null;
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.saveEvent(eventList),
                "Expected saveEvent() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("null or empty"));

        final List<Event<Person>> eventList1 = new ArrayList<>();
        thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.saveEvent(eventList1),
                "Expected saveEvent() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("null or empty"));
    }

    @Test
    @Order(6)
    public void get_eventList_ok() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        assertTrue( eventStore.getEventList(event.getAggregateId(), Person.class).size() == 1);

        event.setRevision(2);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        List<Event<Person>> eventList = eventStore.getEventList(event.getAggregateId().toString(), Person.class);
        assertTrue( eventList.size() == 2);
        //test order by clause
        assertTrue(eventList.get(0).getRevision() == 2);
    }

    @Test
    @Order(7)
    public void get_eventList_not_found() throws Throwable {
        assertTrue( eventStore.getEventList(UUID.randomUUID(), Person.class).size() == 0);

        assertTrue( eventStore.getEventList(UUID.randomUUID().toString(), Person.class).size() == 0);
    }

    @Test
    @Order(6)
    public void get_eventList_from_to_revision_ok() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        assertTrue( eventStore.getEventList(event.getAggregateId(), Person.class).size() == 1);

        event.setRevision(2);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(3);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(4);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(5);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        List<Event<Person>> eventList = eventStore.getEventList(event.getAggregateId().toString(),2,3, Person.class);
        assertTrue( eventList.size() == 2);
        //test order by clause
        assertTrue(eventList.get(0).getRevision() == 3);

        eventList = eventStore.getEventList(event.getAggregateId().toString(),2,5, Person.class);
        assertTrue( eventList.size() == 4);
        //test order by clause
        assertTrue(eventList.get(0).getRevision() == 5);
    }

    @Test
    @Order(7)
    public void get_eventList_from_to_revision_exception() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        assertTrue( eventStore.getEventList(event.getAggregateId(), Person.class).size() == 1);

        event.setRevision(2);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(3);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.getEventList(event.getAggregateId().toString(),3,2, Person.class),
                "Expected getEventList() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("FromRevision"));
    }

    @Test
    @Order(8)
    public void get_LastEvent_ok() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        assertTrue(eventStore.getEventList(event.getAggregateId(), Person.class).size() == 1);

        event.setRevision(2);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(3);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        Event<Person> eventNew = eventStore.getLastEvent(event.getAggregateId().toString(), Person.class);
        assertNotNull(eventNew);
        assertEquals(3, eventNew.getRevision());
    }

    @Test
    @Order(9)
    public void get_LastEvent_not_found() throws Throwable {
        Event<Person> eventNew = eventStore.getLastEvent(UUID.randomUUID(), Person.class);
        assertNull(eventNew);
    }

    @Test
    @Order(10)
    public void get_eventList_from_to_revision_not_found() throws Throwable {
        List<Event<Person>> eventList = eventStore.getEventList(UUID.randomUUID(), Person.class);
        assertEquals(0, eventList.size());
    }

    @Test
    @Order(11)
    public void get_unpublished_events() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        List<Event<Person>> eventList = eventStore.getUnpublishedEventList(Person.class);
        assertTrue(eventList.size() > 0);
    }

    @Test
    @Order(12)
    public void update_to_published_ok() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(2);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        event.setRevision(3);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        int count = eventStore.updateToPublished(event.getAggregateId().toString(), 1,3);
        assertTrue(count == 3);
    }

    @Test
    @Order(13)
    public void update_to_published_not_found() throws Throwable {
                int count = eventStore.updateToPublished(UUID.randomUUID(), 1,3);
        assertTrue(count == 0);
    }


    @Test
    @Order(14)
    public void update_to_published_exception() throws Throwable {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.getEventList(UUID.randomUUID(),3,2, Person.class),
                "Expected getEventList() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("FromRevision"));
    }

    private Event<Person> getPersonEvent(String name, int age) {
        Event<Person> event = new Event<>();
        event.setAggregateId(UUID.randomUUID());
        Person person = new Person(name, age);
        event.setEvent(person);
        event.setRevision(1);
        event.setPublished(false);
        return event;
    }

    static class Person {
        private String name;
        private int age;

        public Person() {

        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
