package ru.tbank.translator.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.tbank.translator.service.TranslationService;

@Controller
@RequestMapping("/translate")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @GetMapping
    public String showTranslationForm(Model model) {
        model.addAttribute("languages", translationService.getSupportedLanguages());

        return "translate";
    }

    @PostMapping
    public String translate(@RequestParam String text,
                            @RequestParam String sourceLang,
                            @RequestParam String targetLang,
                            HttpServletRequest request,
                            Model model) {
        String ipAddress = request.getRemoteAddr();
        String translatedText = translationService.translateText(text, sourceLang, targetLang, ipAddress);
        model.addAttribute("translatedText", translatedText);

        return "result";
    }
}
