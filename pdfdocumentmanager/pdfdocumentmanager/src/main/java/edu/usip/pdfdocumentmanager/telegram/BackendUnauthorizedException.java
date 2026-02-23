package edu.usip.pdfdocumentmanager.telegram;

public class BackendUnauthorizedException extends RuntimeException {
    public BackendUnauthorizedException(String message) {
        super(message);
    }
}