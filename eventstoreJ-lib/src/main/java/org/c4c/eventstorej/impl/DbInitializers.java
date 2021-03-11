package org.c4c.eventstorej.impl;

import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.DbInitializer;

import java.sql.Connection;

class DbInitializers {
    public static DbInitializer getInstance(final StoreType type, final Connection connection, String namespace){

        switch (type){
            case POSTGRES:{
                return new PgDbInitializerImpl(connection, namespace);
            }
            default:
                return null;
        }
    }
}
