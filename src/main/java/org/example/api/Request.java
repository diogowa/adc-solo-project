package org.example.api;

public class Request {
    public static boolean notEmptyOrBlank(String field) {
        return field != null && !field.isBlank();
    }
}
