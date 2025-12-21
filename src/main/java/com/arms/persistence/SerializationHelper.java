package com.arms.persistence;

import com.arms.util.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SerializationHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            Logger.error("Failed to serialize object to JSON", e);
            return null;
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            Logger.error("Failed to deserialize object from JSON", e);
            return null;
        }
    }
    
    public static <T> T fromJson(Path filePath, Class<T> clazz) {
        try {
            return objectMapper.readValue(filePath.toFile(), clazz);
        } catch (IOException e) {
            Logger.error("Failed to deserialize object from file: " + filePath, e);
            return null;
        }
    }
    
    public static void toJsonFile(Object object, Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());
            objectMapper.writeValue(filePath.toFile(), object);
        } catch (IOException e) {
            Logger.error("Failed to serialize object to file: " + filePath, e);
        }
    }
    
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    public static String prettyPrint(String json) {
        try {
            Object jsonNode = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            return json;
        }
    }
}
