package de.thro.importer.strategies;

public class MessageSendException extends RuntimeException{
    public MessageSendException(String message, Throwable cause){
        super(message, cause);
    }
}
