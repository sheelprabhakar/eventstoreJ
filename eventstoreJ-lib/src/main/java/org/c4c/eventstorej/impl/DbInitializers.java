package org.c4c.eventstorej.impl;

import org.apache.commons.lang3.StringUtils;
import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.DbInitializer;

import java.sql.Connection;

class DbInitializers {
    public static DbInitializer getInstance(final StoreType type, final Connection connection, String namespace){
        if(connection == null){
            throw new NullPointerException("Connection can not be null");
        }
        if(StringUtils.isEmpty( namespace)){
            throw new NullPointerException("Namespace can not be null or empty");
        }
        switch (type){
            case POSTGRES:{
                return new PgDbInitializerImpl(connection, namespace);
            }
            case MYSQL:{
                    return new MySqlDbInitializerImpl(connection, namespace);
                }
            default:
                return null;
        }
    }
}
