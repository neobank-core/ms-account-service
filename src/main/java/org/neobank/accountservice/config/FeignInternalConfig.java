package org.neobank.accountservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignInternalConfig {

    @Bean
    public RequestInterceptor internalApiKeyInterceptor(@Value("${neobank.internal-api-key:secret-internal-key-123}") String internalApiKey) {
        return template -> template.header("X-Internal-Api-Key", internalApiKey);
    }
}
