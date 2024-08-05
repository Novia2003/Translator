package ru.tbank.translator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.tbank.translator.configuration.YandexProperties;
import ru.tbank.translator.dto.language.Language;
import ru.tbank.translator.dto.language.LanguagesResponse;
import ru.tbank.translator.dto.translation.TranslationResponse;
import ru.tbank.translator.exception.LanguageNotFoundException;
import ru.tbank.translator.exception.TranslationException;
import ru.tbank.translator.exception.TranslationServiceException;
import ru.tbank.translator.model.TranslationModel;
import ru.tbank.translator.repository.TranslationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class TranslationService {

    private static final int MAX_THREADS = 10;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TranslationRepository repository;

    @Autowired
    private YandexProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);

    public String translateText(String inputText, String sourceLang, String targetLang, String ipAddress) {
        validateLanguages(sourceLang, targetLang);

        String[] words = inputText.split(" ");
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        List<Future<String>> futures = submitTranslationTasks(words, sourceLang, targetLang, executor);
        String result = collectTranslatedText(futures, executor);

        TranslationModel translationModel = new TranslationModel(ipAddress, inputText, result);
        repository.saveTranslation(translationModel);

        return result;
    }

    private void validateLanguages(String sourceLang, String targetLang) {
        if (sourceLang == null || sourceLang.isEmpty())
            throw new LanguageNotFoundException("Source language is not specified");

        if (targetLang == null || targetLang.isEmpty())
            throw new LanguageNotFoundException("Target language is not specified");

        List<String> supportedLanguages;
        try {
            supportedLanguages = getSupportedLanguagesCodes();
        } catch (TranslationServiceException e) {
            throw new LanguageNotFoundException("Failed to get supported languages", e);
        }

        if (!supportedLanguages.contains(sourceLang))
            throw new LanguageNotFoundException("Source language is not supported: " + sourceLang);

        if (!supportedLanguages.contains(targetLang))
            throw new LanguageNotFoundException("Target language is not supported: " + targetLang);
    }

    private List<Future<String>> submitTranslationTasks(String[] words, String sourceLang, String targetLang, ExecutorService executor) {
        List<Future<String>> futures = new ArrayList<>();

        for (String word : words) {
            Future<String> future = executor.submit(() -> {
                try {
                    logger.info("Translating word: {}", word);
                    return translateWord(word, sourceLang, targetLang);
                } catch (TranslationException e) {
                    logger.error("TranslationException occurred: ", e);
                    throw e;
                } catch (Exception e) {
                    logger.error("Exception occurred: ", e);
                    throw new TranslationServiceException("Failed to translate word: " + word, e);
                }
            });
            futures.add(future);
        }

        return futures;
    }

    private String collectTranslatedText(List<Future<String>> futures, ExecutorService executor) {
        StringBuilder translatedText = new StringBuilder();

        for (Future<String> future : futures) {
            try {
                translatedText.append(future.get()).append(" ");
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Exception occurred while translating text: ", e);
                executor.shutdownNow();
                throw new TranslationServiceException("Failed to translate text", e);
            }
        }

        executor.shutdown();
        return translatedText.toString().trim();
    }

    private String translateWord(String word, String sourceLang, String targetLang) {
        HttpHeaders headers = createHeaders();

        Map<String, Object> body = Map.of(
                "sourceLanguageCode", sourceLang,
                "targetLanguageCode", targetLang,
                "texts", List.of(word)
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = properties.getUrl() + "/translate/v2/translate";

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            logger.error("Exception occurred while translating word: ", e);
            throw new TranslationServiceException("Failed to translate word: " + word, e);
        }

        logger.info("Response from translation service: {}", response);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            return handleSuccessfulResponse(response.getBody());

        if (response.getStatusCode().is4xxClientError())
            throw new LanguageNotFoundException("Language not found for word: " + word);

        throw new TranslationServiceException("Failed to translate word: " + word);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + properties.getApiKey());
        return headers;
    }

    private String handleSuccessfulResponse(String responseBody) {
        TranslationResponse translationResponse;
        try {
            translationResponse = objectMapper.readValue(responseBody, TranslationResponse.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse translation response: ", e);
            throw new TranslationServiceException("Failed to parse translation response", e);
        }

        if (translationResponse != null && translationResponse.getTranslations() != null
                && !translationResponse.getTranslations().isEmpty())
            return translationResponse.getTranslations().get(0).getText();

        throw new TranslationServiceException("No translations found in the response");
    }

    public ResponseEntity<String> getSupportedLanguages() {
        HttpHeaders headers = createHeaders();

        String url = properties.getUrl() + "/translate/v2/languages";

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            logger.info("Response from supported languages service: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Exception occurred while fetching supported languages: ", e);
            throw new TranslationServiceException("Failed to fetch supported languages", e);
        }
    }

    public List<String> getSupportedLanguagesCodes() {
        ResponseEntity<String> response = getSupportedLanguages();

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                LanguagesResponse languagesResponse = objectMapper.readValue(response.getBody(), LanguagesResponse.class);

                return languagesResponse.getLanguages().stream()
                        .map(Language::getCode)
                        .collect(Collectors.toList());
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse JSON response: ", e);
                throw new TranslationServiceException("Failed to parse JSON response", e);
            }
        }

        throw new TranslationServiceException("Failed to get a list of languages");
    }
}

