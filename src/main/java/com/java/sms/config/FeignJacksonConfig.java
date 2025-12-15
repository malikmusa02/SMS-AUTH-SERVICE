package com.java.sms.config;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.codec.Decoder;
import feign.codec.Encoder;

import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Jackson configuration used by @FeignClient(configuration = FeignJacksonConfig.class).
 *
 * - Sets snake_case mapping (Django uses snake_case JSON).
 * - Registers JavaTimeModule so java.time.* types are handled.
 * - Ignores unknown properties to be tolerant of extra fields from Django.
 * - Provides Jackson Encoder / Decoder for Feign.
 */
@Configuration
public class FeignJacksonConfig {

    @Bean
    public ObjectMapper feignObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // You can also configure date/time format here if required:
         mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public Decoder feignDecoder(ObjectMapper feignObjectMapper) {
        return new JacksonDecoder(feignObjectMapper);
    }

    @Bean
    public Encoder feignEncoder(ObjectMapper feignObjectMapper) {
        return new JacksonEncoder(feignObjectMapper);
    }
}
