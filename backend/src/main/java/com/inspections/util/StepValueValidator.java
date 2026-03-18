package com.inspections.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Valida valueJson según tipo de step. Alineado con docs/step-types-contract.md y Android StepValidator.
 */
public final class StepValueValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TYPE_BINARY = "BINARY";
    private static final String TYPE_DATE_RANGE = "DATE_RANGE";
    private static final String TYPE_SIMPLE_VALUE = "SIMPLE_VALUE";
    private static final String TYPE_NUMERIC_RANGE = "NUMERIC_RANGE";
    private static final String TYPE_RANGE = "RANGE";
    private static final String TYPE_MULTI_VALUE = "MULTI_VALUE";

    private StepValueValidator() {}

    /**
     * Indica si el valueJson es válido para el step dado.
     */
    public static boolean isValid(String valueJson, String testStepType, Double minValue, Double maxValue) {
        if (valueJson == null || valueJson.isBlank()) return false;
        String type = resolveType(testStepType, minValue, maxValue);
        try {
            return switch (type) {
                case TYPE_BINARY -> isValidBinary(valueJson);
                case TYPE_DATE_RANGE -> isValidDateRange(valueJson);
                case TYPE_SIMPLE_VALUE -> isValidSimpleValue(valueJson);
                case TYPE_NUMERIC_RANGE, TYPE_RANGE -> isValidNumericRange(valueJson, minValue, maxValue);
                case TYPE_MULTI_VALUE -> isValidMultiValue(valueJson);
                default -> !valueJson.isBlank();
            };
        } catch (Exception e) {
            return false;
        }
    }

    private static String resolveType(String testStepType, Double minValue, Double maxValue) {
        if (TYPE_RANGE.equals(testStepType) && (minValue != null || maxValue != null)) return TYPE_NUMERIC_RANGE;
        if (TYPE_RANGE.equals(testStepType)) return TYPE_NUMERIC_RANGE;
        return testStepType != null ? testStepType : TYPE_SIMPLE_VALUE;
    }

    private static boolean isValidBinary(String valueJson) {
        JsonNode node = parse(valueJson);
        if (node == null || !node.has("value")) return false;
        return node.get("value").isBoolean();
    }

    private static boolean isValidDateRange(String valueJson) {
        JsonNode node = parse(valueJson);
        if (node == null) return false;
        String from = node.has("from") ? node.get("from").asText("").trim() : "";
        String to = node.has("to") ? node.get("to").asText("").trim() : "";
        if (from.isEmpty() || to.isEmpty()) return false;
        try {
            LocalDate dFrom = LocalDate.parse(from);
            LocalDate dTo = LocalDate.parse(to);
            return !dFrom.isAfter(dTo);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isValidSimpleValue(String valueJson) {
        JsonNode node = parse(valueJson);
        if (node == null || !node.has("value")) return false;
        String val = node.get("value").asText("").trim();
        return !val.isEmpty();
    }

    private static boolean isValidNumericRange(String valueJson, Double min, Double max) {
        JsonNode node = parse(valueJson);
        if (node == null || !node.has("value")) return false;
        if (!node.get("value").isNumber()) return false;
        double val = node.get("value").asDouble();
        if (min != null && val < min) return false;
        if (max != null && val > max) return false;
        return true;
    }

    private static boolean isValidMultiValue(String valueJson) {
        JsonNode node = parse(valueJson);
        if (node == null || !node.has("values") || !node.get("values").isArray()) return false;
        JsonNode arr = node.get("values");
        if (arr.size() < 3) return false;
        for (JsonNode item : arr) {
            if (!item.has("value")) return false;
            String v = item.get("value").asText("").trim();
            if (v.isEmpty()) return false;
        }
        return true;
    }

    private static JsonNode parse(String valueJson) {
        try {
            return MAPPER.readTree(valueJson);
        } catch (Exception e) {
            return null;
        }
    }
}
