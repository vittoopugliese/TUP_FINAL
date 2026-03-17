package com.example.tup_final.util;

/**
 * Constantes unificadas para Steps (Test Steps).
 * Contrato único entre .docx, docs, Room y backend.
 * <p>
 * Tipos de input editables (T5.1.2):
 * - BINARY: Sí/No dropdown
 * - DATE_RANGE: Dos selectores de fecha (from/to)
 * - SIMPLE_VALUE: Texto, número o fecha simple
 * - NUMERIC_RANGE: Valor numérico con validación min/max
 * - MULTI_VALUE: Múltiples subcampos
 * <p>
 * Tipos excluidos en esta iteración:
 * - AUTOMATIC: Solo lectura, fuera de alcance
 * - RANGE: Legacy, mapear a NUMERIC_RANGE cuando minValue/maxValue son numéricos
 */
public final class StepConstants {

    private StepConstants() {}

    // ── Step Types (testStepType) ─────────────────────────────────────────────

    public static final String TYPE_BINARY = "BINARY";
    public static final String TYPE_DATE_RANGE = "DATE_RANGE";
    public static final String TYPE_SIMPLE_VALUE = "SIMPLE_VALUE";
    public static final String TYPE_NUMERIC_RANGE = "NUMERIC_RANGE";
    /** Legacy: usar cuando minValue/maxValue son numéricos. */
    public static final String TYPE_RANGE = "RANGE";
    public static final String TYPE_MULTI_VALUE = "MULTI_VALUE";
    /** Fuera de alcance T5.1.2 - no implementar como editable. */
    public static final String TYPE_AUTOMATIC = "AUTOMATIC";

    // ── Step Status ──────────────────────────────────────────────────────────

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    /** Legacy: mapear a COMPLETED al leer. */
    public static final String STATUS_SUCCESS_LEGACY = "SUCCESS";

    // ── Test Status ──────────────────────────────────────────────────────────

    public static final String TEST_STATUS_PENDING = "PENDING";
    public static final String TEST_STATUS_COMPLETED = "COMPLETED";
    public static final String TEST_STATUS_FAILED = "FAILED";

    // ── Value Types (valueType en valueJson) ──────────────────────────────────

    public static final String VALUE_TYPE_BOOLEAN = "BOOLEAN_VALUE";
    public static final String VALUE_TYPE_STRING = "STRING_VALUE";
    public static final String VALUE_TYPE_NUMERIC = "NUMERIC_VALUE";
    public static final String VALUE_TYPE_NUMERIC_UNIT = "NUMERIC_UNIT_VALUE";
    public static final String VALUE_TYPE_DATE = "DATE_VALUE";
    public static final String VALUE_TYPE_DATE_RANGE = "DATE_RANGE_VALUE";

    /**
     * Normaliza estado legacy SUCCESS a COMPLETED.
     */
    public static String normalizeStepStatus(String status) {
        if (STATUS_SUCCESS_LEGACY.equals(status)) {
            return STATUS_COMPLETED;
        }
        return status != null ? status : STATUS_PENDING;
    }

    /**
     * Mapea RANGE legacy a NUMERIC_RANGE cuando el step tiene minValue/maxValue numéricos.
     */
    public static String resolveStepType(String testStepType, Double minValue, Double maxValue) {
        if (TYPE_RANGE.equals(testStepType) && (minValue != null || maxValue != null)) {
            return TYPE_NUMERIC_RANGE;
        }
        if (TYPE_RANGE.equals(testStepType)) {
            return TYPE_NUMERIC_RANGE; // Por defecto
        }
        return testStepType != null ? testStepType : TYPE_SIMPLE_VALUE;
    }
}
