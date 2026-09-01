// src/main/java/com/waraloyer/client/exception/MessageLimitExceededException.java

package com.waraloyer.client.exception;

public class MessageLimitExceededException extends RuntimeException {
    public MessageLimitExceededException(String message) {
        super(message);
    }
}