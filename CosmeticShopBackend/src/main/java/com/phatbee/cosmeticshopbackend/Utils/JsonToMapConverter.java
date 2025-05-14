package com.phatbee.cosmeticshopbackend.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Converter
public class JsonToMapConverter implements AttributeConverter<Map<String, Object>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public String convertToDatabaseColumn(Map<String, Object> stringObjectMap) {
        try {
            return objectMapper.writeValueAsString(stringObjectMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}"; // Fallback to empty JSON object if serialization fails
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String s) {
        try {
            return objectMapper.readValue(s, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>(); // Fallback to empty map if deserialization fails
        }
    }
}
