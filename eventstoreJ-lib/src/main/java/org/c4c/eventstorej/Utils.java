package org.c4c.eventstorej;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.regex.Pattern;

public class Utils {
    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T convertJsonStringToObject(final String jsonStr,
                                                  final Class<T> classType) throws IOException {
        return mapper.readValue(jsonStr, classType);
    }

    public static byte[] convertObjectToJsonStream(final Object object)
            throws JsonProcessingException {
        return mapper.writeValueAsBytes(object);
    }

    public static String convertObjectToJsonString(final Object object)
            throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

}
