package com.fz.springboot.starter.jpa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author fbb
 * @version 1.0
 * @since 2020/1/6/006 17:22
 */

@Converter
public class StringSplitterConverter implements AttributeConverter<List<String>, String> {
    
    private static final String DELIMITER = ",";
    
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return String.join(DELIMITER, attribute);
    }
    
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(dbData.split(DELIMITER));
    }
}