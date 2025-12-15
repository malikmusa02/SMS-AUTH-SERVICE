package com.java.sms.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.Map;

@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> map) {
        try {
            return map == null ? null : objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting map to JSON", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String json) {
        try {
            return json == null ? null : objectMapper.readValue(json, Map.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting JSON to map", e);
        }
    }
}
