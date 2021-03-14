package org.c4c.eventstorej.impl;

import org.c4c.eventstorej.EventStoreJException;
import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.EventStore;

import java.sql.Connection;


public class EventStoreFactory {

    public static EventStore getStore(final Connection conn, final StoreType type, String namespace) throws EventStoreJException {

        switch (type){
            case POSTGRES:{
                return new PgEventStoreJImpl(conn, DbInitializers.getInstance( type, conn, namespace), namespace);
            }
        }
        throw new EventStoreJException("Can not initialize store");
    }
}
