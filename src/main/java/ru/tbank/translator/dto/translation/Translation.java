package ru.tbank.translator.dto.translation;

public class Translation {

    private String text;

    public Translation() {
    }

    public Translation(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
