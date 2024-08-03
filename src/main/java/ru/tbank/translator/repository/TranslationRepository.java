package ru.tbank.translator.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TranslationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveTranslation(String ipAddress, String inputText, String translatedText) {
        String sql = "INSERT INTO translations (ip_address, input_text, translated_text) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, ipAddress, inputText, translatedText);
    }
}
