package org.example;

public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException() {
        super("Room has active sensors and cannot be deleted.");
    }
}

