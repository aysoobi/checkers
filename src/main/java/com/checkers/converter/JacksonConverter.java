package com.checkers.converter;

import com.checkers.model.Move;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class JacksonConverter implements AttributeConverter<List<Move>, String> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<List<Move>> MOVE_LIST_TYPE = new TypeReference<>() {};

  @Override
  public String convertToDatabaseColumn(List<Move> attribute) {
    if (attribute == null) {
      return null;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to serialize moves to JSON", e);
    }
  }

  @Override
  public List<Move> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(dbData, MOVE_LIST_TYPE);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to deserialize moves from JSON", e);
    }
  }
}
