package ru.tbank.translator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.tbank.translator.configuration.YandexProperties;
import ru.tbank.translator.dto.language.Language;
import ru.tbank.translator.dto.language.LanguagesResponse;
import ru.tbank.translator.exception.LanguageNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TranslationServiceTest {

    @InjectMocks
    private TranslationService translationService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private YandexProperties properties;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTranslateTextLanguageNotFound() throws JsonProcessingException {
        String inputText = "hello world";
        String sourceLang = "en";
        String targetLang = "ru";
        String ipAddress = "127.0.0.1";

        when(properties.getUrl()).thenReturn("http://example.com");
        when(properties.getApiKey()).thenReturn("api-key");

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"error\":\"Language not found\"}", HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(responseEntity);

        LanguagesResponse languagesResponse = new LanguagesResponse();
        languagesResponse.setLanguages(List.of(new Language("en"), new Language("ru")));
        when(objectMapper.readValue(anyString(), eq(LanguagesResponse.class))).thenReturn(languagesResponse);

        assertThrows(LanguageNotFoundException.class, () -> {
            translationService.translateText(inputText, sourceLang, targetLang, ipAddress);
        });
    }


    @Test
    public void testGetSupportedLanguages() {
        when(properties.getUrl()).thenReturn("http://example.com");
        when(properties.getApiKey()).thenReturn("api-key");

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"languages\":[{\"code\":\"en\"},{\"code\":\"ru\"}]}", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(responseEntity);

        ResponseEntity<String> result = translationService.getSupportedLanguages();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("{\"languages\":[{\"code\":\"en\"},{\"code\":\"ru\"}]}", result.getBody());
    }

    @Test
    public void testGetSupportedLanguagesCodes() throws JsonProcessingException {
        when(properties.getUrl()).thenReturn("http://example.com");
        when(properties.getApiKey()).thenReturn("api-key");

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"languages\":[{\"code\":\"en\"},{\"code\":\"ru\"}]}", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(responseEntity);

        LanguagesResponse languagesResponse = new LanguagesResponse();
        languagesResponse.setLanguages(List.of(new Language("en"), new Language("ru")));
        when(objectMapper.readValue(anyString(), eq(LanguagesResponse.class))).thenReturn(languagesResponse);

        List<String> result = translationService.getSupportedLanguagesCodes();

        assertEquals(List.of("en", "ru"), result);
    }
}
