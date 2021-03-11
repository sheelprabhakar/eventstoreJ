package org.c4c.eventstorej.example;
import org.c4c.eventstorej.EventStoreJException;
import org.c4c.eventstorej.StoreType;
import org.c4c.eventstorej.api.EventStore;
import org.c4c.eventstorej.impl.EventStoreFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class DbConfig {
    @Value("${db.url}")
    private String dbUrl;

    @Value("${user}")
    private String userName;

    @Value("${password}")
    private String password;

    @Bean
    public Connection connection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        Properties props = new Properties();
        props.setProperty("user", userName);
        props.setProperty("password",password);
        return DriverManager.getConnection(dbUrl, props);
    }
    @Bean
    public EventStore eventStore(final Connection conn) throws EventStoreJException {
        return EventStoreFactory.getStore(conn, StoreType.POSTGRES, "name");
    }
}
