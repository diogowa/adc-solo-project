package org.example.util;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.Map;

public class ResponseHelper {
    public static final String INVALID_CREDENTIALS = "The username-password pair is not valid";
    public static final String USER_ALREADY_EXISTS = "Error in creating an account because the username already exists";
    public static final String USER_NOT_FOUND = "The username referred in the operation doesn’t exist in registered accounts";
    public static final String INVALID_TOKEN = "The operation is called with an invalid token (wrong format for example)";
    public static final String TOKEN_EXPIRED = "The operation is called with a token that is expired";
    public static final String UNAUTHORIZED = "The operation is not allowed for the user role";
    public static final String INVALID_INPUT = "The call is using input data not following the correct specification";
    public static final String FORBIDDEN = "The operation generated a forbidden error by other reason";
    public static final String INTERNAL_SERVER_ERROR = "An error occurred while processing the request";

    public static Response ok(Map<String, Object> entity) {
        return Response.ok(Map.of("status", "success", "data", entity)).build();
    }

    public static Response error(String message) {
        String messageCode = getMessageCode(message);
        return Response.status(getHttpStatus(messageCode))
                .entity(Map.of("status", messageCode, "data", message))
                .build();
    }

    private static String getMessageCode(String message) {
        return switch (message) {
            case INVALID_CREDENTIALS -> "9900";
            case USER_ALREADY_EXISTS -> "9901";
            case USER_NOT_FOUND -> "9902";
            case INVALID_TOKEN -> "9903";
            case TOKEN_EXPIRED -> "9904";
            case UNAUTHORIZED -> "9905";
            case INVALID_INPUT -> "9906";
            case FORBIDDEN -> "9907";
            default -> "500";
        };
    }

    private static int getHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "9900", "9903", "9904" -> Status.UNAUTHORIZED.getStatusCode();
            case "9901" -> Status.CONFLICT.getStatusCode();
            case "9902" -> Status.NOT_FOUND.getStatusCode();
            case "9905", "9907" -> Status.FORBIDDEN.getStatusCode();
            case "9906" -> Status.BAD_REQUEST.getStatusCode();
            default -> Status.INTERNAL_SERVER_ERROR.getStatusCode();
        };
    }
}
