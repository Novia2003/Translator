package ru.tbank.translator.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.tbank.translator.service.TranslationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TranslationControllerTest {

    @InjectMocks
    private TranslationController translationController;

    @Mock
    private TranslationService translationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetLanguages() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"languages\":[{\"code\":\"en\"},{\"code\":\"ru\"}]}", HttpStatus.OK);
        when(translationService.getSupportedLanguages()).thenReturn(responseEntity);

        ResponseEntity<String> result = translationController.getLanguages();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("{\"languages\":[{\"code\":\"en\"},{\"code\":\"ru\"}]}", result.getBody());
    }

    @Test
    public void testTranslate() {
        String text = "hello world";
        String sourceLanguageCode = "en";
        String targetLanguageCode = "ru";
        String ipAddress = "127.0.0.1";

        when(translationService.translateText(text, sourceLanguageCode, targetLanguageCode, ipAddress)).thenReturn("привет мир");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(ipAddress);

        String result = translationController.translate(text, sourceLanguageCode, targetLanguageCode, request);

        assertEquals("привет мир", result);
    }
}
