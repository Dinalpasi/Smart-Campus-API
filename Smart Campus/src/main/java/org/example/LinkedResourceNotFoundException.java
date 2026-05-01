package org.example;

public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException() {
        super("The specified roomId does not exist.");
    }
}

