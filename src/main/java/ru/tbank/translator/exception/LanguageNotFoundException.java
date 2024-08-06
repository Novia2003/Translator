package ru.tbank.translator.exception;

public class LanguageNotFoundException extends TranslationException {

    public LanguageNotFoundException(String message) {
        super(message);
    }

    public LanguageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}