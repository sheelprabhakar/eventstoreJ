package org.c4c.eventstorej;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    private static ObjectMapper mapper = new ObjectMapper();

    public static String objectToString(Object obj) throws Throwable {
       return mapper.writeValueAsString(obj);
    }
}
