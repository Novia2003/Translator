package ru.tbank.translator.dto.language;

import java.util.List;

public class LanguagesResponse {

    private List<Language> languages;

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }
}
