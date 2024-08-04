package ru.tbank.translator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.tbank.translator.configuration.YandexProperties;
import ru.tbank.translator.exception.LanguageNotFoundException;
import ru.tbank.translator.exception.TranslationException;
import ru.tbank.translator.exception.TranslationServiceException;
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

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);

    public String translateText(String inputText, String sourceLang, String targetLang, String ipAddress) {
        if (sourceLang == null || sourceLang.isEmpty())
            throw new LanguageNotFoundException("Source language is not specified");

        if (targetLang == null || targetLang.isEmpty())
            throw new LanguageNotFoundException("Target language is not specified");

        List<String> supportedLanguages = getSupportedLanguages();

        if (!supportedLanguages.contains(sourceLang))
            throw new LanguageNotFoundException("Source language is not supported: " + sourceLang);

        if (!supportedLanguages.contains(targetLang))
            throw new LanguageNotFoundException("Target language is not supported: " + targetLang);

        String[] words = inputText.split(" ");
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

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

        StringBuffer translatedText = new StringBuffer();

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

        String result = translatedText.toString().trim();

        repository.saveTranslation(ipAddress, inputText, result);

        return result;
    }

    private String translateWord(String word, String sourceLang, String targetLang) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + properties.getApiKey());

        Map<String, Object> body = Map.of(
                "sourceLanguageCode", sourceLang,
                "targetLanguageCode", targetLang,
                "texts", List.of(word)
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = properties.getUrl() + "/translate/v2/translate";

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        logger.info("Response from translation service: {}", response);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();

            if (responseBody.containsKey("translations")) {
                @SuppressWarnings("unchecked")
                Map<String, String> translation = ((List<Map<String, String>>) responseBody.get("translations")).get(0);

                return translation.get("text");
            }
        }

        if (response.getStatusCode().is4xxClientError()) {
            throw new LanguageNotFoundException("Language not found for word: " + word);
        }

        throw new TranslationServiceException("Failed to translate word: " + word);
    }

    public List<String> getSupportedLanguages() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + properties.getApiKey());

        String url = properties.getUrl() + "/translate/v2/languages";

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();

            if (responseBody.containsKey("languages")) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> languagesMap = (List<Map<String, String>>) responseBody.get("languages");

                return languagesMap.stream()
                        .map(a -> a.get("code"))
                        .collect(Collectors.toList());
            }
        }

        throw new TranslationServiceException("Failed to get a list of languages");
    }
}

