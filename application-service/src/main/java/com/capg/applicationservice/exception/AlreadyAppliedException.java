package com.capg.applicationservice.exception;

public class AlreadyAppliedException extends RuntimeException {

    public AlreadyAppliedException(String message) {
        super(message);
    }
}