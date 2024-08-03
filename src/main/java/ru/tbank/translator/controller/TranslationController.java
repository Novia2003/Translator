package ru.tbank.translator.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.tbank.translator.service.TranslationService;

@RestController
@RequestMapping("/translate")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @PostMapping
    public String translate(@RequestParam String text,
                            @RequestParam String sourceLang,
                            @RequestParam String targetLang,
                            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        return translationService.translateText(text, sourceLang, targetLang, ipAddress);
    }
}

