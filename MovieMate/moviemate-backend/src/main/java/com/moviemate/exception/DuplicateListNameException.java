package com.moviemate.exception;

public class DuplicateListNameException extends RuntimeException {
    public DuplicateListNameException(String name) {
        super("Ya tienes una lista con el nombre '" + name + "'");
    }
}
