package ru.tbank.translator.dto.language;

public class Language {

    private String code;

    private String name;

    public Language() {
    }

    public Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}