package com.example.solveitwebsitebackend.exceptions;

public class DAOExceptions {

    public static class FetchException extends RuntimeException {
        public FetchException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
