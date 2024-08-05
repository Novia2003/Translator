package ru.tbank.translator.model;

public class TranslationModel {

    private String ipAddress;

    private String inputText;

    private String translatedText;

    public TranslationModel(String ipAddress, String inputText, String translatedText) {
        this.ipAddress = ipAddress;
        this.inputText = inputText;
        this.translatedText = translatedText;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
