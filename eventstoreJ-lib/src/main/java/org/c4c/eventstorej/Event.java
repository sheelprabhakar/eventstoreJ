package org.c4c.eventstorej;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.UUID;

public class Event<T> implements Serializable {
    private BigInteger position;
    private UUID aggregateId;
    private Integer revision = -1;
    private T event;
    private boolean published;

    public BigInteger getPosition() {
        return position;
    }

    public void setPosition(BigInteger position) {
        this.position = position;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public T getEvent() {
        return event;
    }

    public void setEvent(T event) {
        this.event = event;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getEventString() throws Throwable {
        return Utils.convertObjectToJsonString(this.event);
    }

}
