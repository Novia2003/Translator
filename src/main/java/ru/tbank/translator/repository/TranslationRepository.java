package ru.tbank.translator.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tbank.translator.model.TranslationModel;

@Repository
public class TranslationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveTranslation(TranslationModel translationModel) {
        String sql = "INSERT INTO translations (ip_address, input_text, translated_text) VALUES (?, ?, ?)";
        jdbcTemplate.update(
                sql,
                translationModel.getIpAddress(),
                translationModel.getInputText(),
                translationModel.getTranslatedText()
        );
    }
}
