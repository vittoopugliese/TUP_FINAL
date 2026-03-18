package com.example.tup_final.ui.steps;

import com.example.tup_final.util.StepConstants;

import org.json.JSONObject;

/**
 * Valida steps para habilitar/deshabilitar el botón Completar.
 */
public final class StepValidator {

    private StepValidator() {}

    /**
     * Indica si todos los steps aplicables están completos y válidos.
     */
    public static boolean canCompleteTest(java.util.List<StepUiModel> steps) {
        if (steps == null || steps.isEmpty()) return false;
        for (StepUiModel s : steps) {
            if (!s.applicable) continue;
            if (!isStepValid(s)) return false;
        }
        return true;
    }

    /**
     * Valida valueJson por tipo (para fallback Room/backend alignment).
     */
    public static boolean isValueValid(String valueJson, String testStepType, Double minValue, Double maxValue) {
        if (valueJson == null || valueJson.trim().isEmpty()) return false;
        String type = testStepType;
        if (StepConstants.TYPE_RANGE.equals(type)) type = StepConstants.TYPE_NUMERIC_RANGE;
        if (type == null) type = StepConstants.TYPE_SIMPLE_VALUE;
        switch (type) {
            case StepConstants.TYPE_BINARY:
                return StepValueMapper.parseBinaryValue(valueJson) != null;
            case StepConstants.TYPE_DATE_RANGE:
                return isDateRangeValid(valueJson);
            case StepConstants.TYPE_SIMPLE_VALUE:
                String sv = StepValueMapper.parseStringValue(valueJson);
                return sv != null && !sv.trim().isEmpty();
            case StepConstants.TYPE_NUMERIC_RANGE:
                return isNumericRangeValid(valueJson, minValue, maxValue);
            case StepConstants.TYPE_MULTI_VALUE:
                return isMultiValueValid(valueJson);
            default:
                return true;
        }
    }

    /**
     * Indica si un step aplicable tiene valor válido.
     */
    public static boolean isStepValid(StepUiModel step) {
        if (!step.applicable) return true;
        if (step.valueJson == null || step.valueJson.trim().isEmpty()) return false;
        switch (step.testStepType) {
            case StepConstants.TYPE_BINARY:
                return StepValueMapper.parseBinaryValue(step.valueJson) != null;
            case StepConstants.TYPE_DATE_RANGE:
                return isDateRangeValid(step.valueJson);
            case StepConstants.TYPE_SIMPLE_VALUE:
                String sv = StepValueMapper.parseStringValue(step.valueJson);
                return sv != null && !sv.trim().isEmpty();
            case StepConstants.TYPE_NUMERIC_RANGE:
            case StepConstants.TYPE_RANGE:
                return isNumericRangeValid(step.valueJson, step.minValue, step.maxValue);
            case StepConstants.TYPE_MULTI_VALUE:
                return isMultiValueValid(step.valueJson);
            default:
                return step.valueJson != null && !step.valueJson.trim().isEmpty();
        }
    }

    private static boolean isDateRangeValid(String valueJson) {
        if (valueJson == null || valueJson.trim().isEmpty()) return false;
        try {
            JSONObject obj = new JSONObject(valueJson);
            String from = obj.optString("from", "").trim();
            String to = obj.optString("to", "").trim();
            if (from.isEmpty() || to.isEmpty()) return false;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.util.Date dFrom = sdf.parse(from);
            java.util.Date dTo = sdf.parse(to);
            return dFrom != null && dTo != null && !dFrom.after(dTo);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isNumericRangeValid(String valueJson, Double min, Double max) {
        Double val = StepValueMapper.parseNumericValue(valueJson);
        if (val == null) return false;
        if (min != null && val < min) return false;
        if (max != null && val > max) return false;
        return true;
    }

    private static boolean isMultiValueValid(String valueJson) {
        String[] vals = StepValueMapper.parseMultiValue(valueJson);
        if (vals == null || vals.length < 3) return false;
        for (String v : vals) {
            if (v == null || v.trim().isEmpty()) return false;
        }
        return true;
    }
}
