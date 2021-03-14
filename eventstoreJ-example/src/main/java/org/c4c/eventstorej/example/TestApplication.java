package org.c4c.eventstorej.example;

import org.c4c.eventstorej.EventStoreJException;
import org.c4c.eventstorej.api.EventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
public class TestApplication implements CommandLineRunner {

    @Autowired
    public EventStore eventStore;

    public static  void main(String[] args){
        SpringApplication.run(TestApplication.class, args);

    }

    @Override
    public void run(String... args) {
        if(this.eventStore != null){
            try {
                this.eventStore.init();
            } catch (EventStoreJException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }


}
