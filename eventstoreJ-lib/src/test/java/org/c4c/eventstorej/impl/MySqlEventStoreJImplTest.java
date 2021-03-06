package org.c4c.eventstorej.impl;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.c4c.eventstore.test.mysql.EmbeddedMySql;
import org.c4c.eventstorej.Event;
import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.EventStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.postgresql.util.PSQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testable
public class MySqlEventStoreJImplTest extends EmbeddedMySql {
    static EventStore eventStore;
    private final static String NAME_SPACE = "event";

    @BeforeAll
    static void initEventStore() {
        eventStore = new MySqlEventStoreJImpl(conn, DbInitializers.getInstance(StoreType.MYSQL, conn, NAME_SPACE), NAME_SPACE);
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
    public void save_event_invalid_params() {
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
    public void save_eventList_invalid_param() {
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

        assertTrue(eventStore.getEventList(event.getAggregateId(), Person.class).size() == 1);

        event.setRevision(2);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        List<Event<Person>> eventList = eventStore.getEventList(event.getAggregateId().toString(), Person.class);
        assertTrue(eventList.size() == 2);
        //test order by clause
        assertTrue(eventList.get(0).getRevision() == 2);
    }

    @Test
    @Order(7)
    public void get_eventList_not_found() throws Throwable {
        assertTrue(eventStore.getEventList(UUID.randomUUID(), Person.class).size() == 0);

        assertTrue(eventStore.getEventList(UUID.randomUUID().toString(), Person.class).size() == 0);
    }

    @Test
    @Order(6)
    public void get_eventList_from_to_revision_ok() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        assertTrue(eventStore.getEventList(event.getAggregateId(), Person.class).size() == 1);

        event.setRevision(2);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(3);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(4);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(5);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        List<Event<Person>> eventList = eventStore.getEventList(event.getAggregateId().toString(), 2, 3, Person.class);
        assertTrue(eventList.size() == 2);
        //test order by clause
        assertTrue(eventList.get(0).getRevision() == 3);

        eventList = eventStore.getEventList(event.getAggregateId().toString(), 2, 5, Person.class);
        assertTrue(eventList.size() == 4);
        //test order by clause
        assertTrue(eventList.get(0).getRevision() == 5);
    }

    @Test
    @Order(7)
    public void get_eventList_from_to_revision_exception() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        assertTrue(eventStore.getEventList(event.getAggregateId(), Person.class).size() == 1);

        event.setRevision(2);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(3);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.getEventList(event.getAggregateId().toString(), 3, 2, Person.class),
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

        int count = eventStore.updateToPublished(event.getAggregateId().toString(), 1, 3);
        assertTrue(count == 3);
    }

    @Test
    @Order(13)
    public void update_to_published_not_found() throws Throwable {
        int count = eventStore.updateToPublished(UUID.randomUUID(), 1, 3);
        assertTrue(count == 0);
    }


    @Test
    @Order(14)
    public void update_to_published_exception() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.getEventList(UUID.randomUUID(), 3, 2, Person.class),
                "Expected getEventList() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("FromRevision"));
    }

    @Test
    @Order(15)
    public void save_snapshot_ok() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(2);
        event.getEvent().setAge(41);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        List<Event<Person>> eventList = eventStore.getEventList(event.getAggregateId(), 1, 2, Person.class);
        event = eventList.get(1);
        assertEquals(1, eventStore.saveSnapShot(event.getAggregateId().toString(), event.getRevision(), event));
    }

    @Test
    @Order(16)
    public void save_snapshot_duplicate_exception() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(2);
        event.getEvent().setAge(41);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        List<Event<Person>> eventList = eventStore.getEventList(event.getAggregateId(), 1, 2, Person.class);
        final Event event1 = eventList.get(1);
        assertEquals(1, eventStore.saveSnapShot(event1.getAggregateId().toString(), event1.getRevision(), event1));


        MySQLIntegrityConstraintViolationException thrown = assertThrows(
                MySQLIntegrityConstraintViolationException.class,
                () -> eventStore.saveSnapShot(event1.getAggregateId().toString(), event1.getRevision(), event1),
                "Expected saveSnapShot() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Duplicate entry"));
    }

    @Test
    @Order(17)
    public void get_snapshot_ok() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        event.setRevision(2);
        event.getEvent().setAge(41);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        List<Event<Person>> eventList = eventStore.getEventList(event.getAggregateId(), 1, 2, Person.class);
        event = eventList.get(1);
        assertEquals(1, eventStore.saveSnapShot(event.getAggregateId().toString(), event.getRevision(), event.getEvent()));

        Person snap = eventStore.getSnapShot(event.getAggregateId().toString(), Person.class);

        assertEquals(event.getEvent().getName(), snap.getName());
    }

    @Test
    @Order(18)
    public void get_snapshot_not_found() throws Throwable {
        Person snap = eventStore.getSnapShot(UUID.randomUUID(), Person.class);
        assertEquals(snap, null);
    }

    @Test
    @Order(19)
    public void get_snapshot_invalid_param() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.getSnapShot("", Person.class),
                "Expected getSnapShot() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("invalid"));

        thrown = assertThrows(
                IllegalArgumentException.class,
                () -> eventStore.getSnapShot("32434", Person.class),
                "Expected getSnapShot() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("invalid"));
    }

    @Test
    @Order(20)
    public void get_replay_ok() throws Throwable {
        Event<Person> event = getPersonEvent("Prabhakar", 40);
        Assertions.assertTrue(eventStore.saveEvent(event) == 1);
        List<Event<Person>> eventList = eventStore.getReplay(0, 1, Person.class);
        assertTrue(eventList.size() > 0);
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


}
