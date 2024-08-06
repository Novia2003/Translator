package ru.tbank.translator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "yandex")
public class YandexProperties {

    private String url;

    private String apiKey;

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
