package ru.tbank.translator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.translator.service.TranslationService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "TranslationController", description = "Функции переводчика")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @GetMapping("/languages")
    @Operation(description = "Просмотр списка поддерживаемых языков и их кодов")
    public ResponseEntity<String> getLanguages() {
        return translationService.getSupportedLanguages();
    }

    @PostMapping("/translate")
    @Operation(description = "Перевод текста с одного языка на другой")
    public String translate(@RequestParam String text,
                            @RequestParam String sourceLanguageCode,
                            @RequestParam String targetLanguageCode,
                            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        return translationService.translateText(text, sourceLanguageCode, targetLanguageCode, ipAddress);
    }
}
