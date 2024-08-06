package ru.tbank.translator.exception;

public class TranslationServiceException extends TranslationException {

    public TranslationServiceException(String message) {
        super(message);
    }

    public TranslationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}