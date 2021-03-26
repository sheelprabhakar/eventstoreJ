package org.c4c.eventstorej.impl;

import org.c4c.eventstorej.api.DbInitializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class MySqlDbInitializerImpl implements DbInitializer {
    private final Connection connection;
    private final String namespace ;
    public MySqlDbInitializerImpl(final Connection connection, String namespace) {
        this.connection = connection;
        this.namespace = namespace;
    }

    @Override
    public void initialize() throws Throwable {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream("mysql.sql")){
            if(is == null){
                throw new NullPointerException("pg.sql file stream null");
            }
            InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder sb= new StringBuilder();
            for (String line; (line = reader.readLine()) != null;) {
                sb.append( line.replace("${namespace}", namespace));
            }
            String script = sb.toString();
            if(this.connection != null){
                Statement st = this.connection.createStatement();
                st.execute(script);
                st.close();
            } else{
                throw new NullPointerException("Connection can not be null");
            }
        } catch (IOException | SQLException e) {
            throw e;
        }

    }
}
