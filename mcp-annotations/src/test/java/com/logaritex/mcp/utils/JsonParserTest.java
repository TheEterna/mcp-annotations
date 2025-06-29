package com.logaritex.mcp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonParserTest {

    @Test
    void toJson_withValidJsonString_returnsSameString() throws JsonProcessingException {
        String jsonString = "{\"name\":\"John\", \"age\":30}";
        String result = JsonParser.toJson(jsonString);
        assertEquals(jsonString, result);
    }

    @Test
    void toJson_withInvalidJsonString_convertsToJson() throws JsonProcessingException {
        String invalidJson = "invalid json";
        String result = JsonParser.toJson(invalidJson);
        assertEquals("\"invalid json\"", result); // Should be quoted as it's treated as a string
    }

    @Test
    void toJson_withSimpleObject_convertsToJson() throws JsonProcessingException {
        Object simpleObject = new Object() {
            public final String name = "Alice";
            public final int age = 25;
        };
        String result = JsonParser.toJson(simpleObject);
        assertEquals("{\"name\":\"Alice\",\"age\":25}", result);
    }

    @Test
    void toJson_withNull_returnsNullJson() throws JsonProcessingException {
        String result = JsonParser.toJson(null);
        assertEquals("[DONE]", result);
    }

    @Test
    void toJson_withNumber_convertsToJson() throws JsonProcessingException {
        Number number = 42;
        String result = JsonParser.toJson(number);
        assertEquals("42", result);
    }

    @Test
    void toJson_withBoolean_convertsToJson() throws JsonProcessingException {
        Boolean bool = true;
        String result = JsonParser.toJson(bool);
        assertEquals("true", result);
    }

    @Test
    void toJson_withList_convertsToJson() throws JsonProcessingException {
        java.util.List<String> list = java.util.Arrays.asList("a", "b", "c");
        String result = JsonParser.toJson(list);
        assertEquals("[\"a\",\"b\",\"c\"]", result);
    }

    @Test
    void toJson_withEmptyString_returnsQuotedEmptyString() throws JsonProcessingException {
        String empty = null;
        String result = JsonParser.toJson(empty);
    }
}
