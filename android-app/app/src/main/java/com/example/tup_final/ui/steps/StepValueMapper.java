package com.example.tup_final.ui.steps;

import com.example.tup_final.util.StepConstants;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Mapea entre valueJson y valores tipados para UI.
 */
public final class StepValueMapper {

    private StepValueMapper() {}

    /**
     * Parsea valueJson a valor booleano para BINARY.
     */
    public static Boolean parseBinaryValue(String valueJson) {
        if (valueJson == null || valueJson.trim().isEmpty()) return null;
        try {
            JSONObject obj = new JSONObject(valueJson);
            if (obj.has("value") && !obj.isNull("value")) {
                return obj.getBoolean("value");
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Crea valueJson para BINARY.
     */
    public static String toBinaryJson(Boolean value) {
        if (value == null) return null;
        try {
            JSONObject obj = new JSONObject();
            obj.put("value", value);
            obj.put("valueType", StepConstants.VALUE_TYPE_BOOLEAN);
            return obj.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parsea valueJson para DATE_RANGE (from, to).
     */
    public static String[] parseDateRangeValue(String valueJson) {
        if (valueJson == null || valueJson.trim().isEmpty()) return null;
        try {
            JSONObject obj = new JSONObject(valueJson);
            String from = obj.optString("from", null);
            String to = obj.optString("to", null);
            if ((from != null && !from.isEmpty()) || (to != null && !to.isEmpty())) {
                return new String[]{from != null ? from : "", to != null ? to : ""};
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Crea valueJson para DATE_RANGE.
     */
    public static String toDateRangeJson(String from, String to) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("from", from != null ? from : "");
            obj.put("to", to != null ? to : "");
            obj.put("valueType", StepConstants.VALUE_TYPE_DATE_RANGE);
            return obj.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parsea valor numérico de valueJson.
     */
    public static Double parseNumericValue(String valueJson) {
        if (valueJson == null || valueJson.trim().isEmpty()) return null;
        try {
            JSONObject obj = new JSONObject(valueJson);
            if (obj.has("value") && !obj.isNull("value")) {
                return obj.getDouble("value");
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Crea valueJson para valor numérico simple.
     */
    public static String toNumericJson(Double value) {
        if (value == null) return null;
        try {
            JSONObject obj = new JSONObject();
            obj.put("value", value);
            obj.put("valueType", StepConstants.VALUE_TYPE_NUMERIC);
            return obj.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parsea valor string de valueJson.
     */
    public static String parseStringValue(String valueJson) {
        if (valueJson == null || valueJson.trim().isEmpty()) return null;
        try {
            JSONObject obj = new JSONObject(valueJson);
            if (obj.has("value") && !obj.isNull("value")) {
                return obj.getString("value");
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Crea valueJson para string.
     */
    public static String toStringJson(String value) {
        if (value == null) return null;
        try {
            JSONObject obj = new JSONObject();
            obj.put("value", value);
            obj.put("valueType", StepConstants.VALUE_TYPE_STRING);
            return obj.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parsea valueJson para MULTI_VALUE (array de valores).
     * Retorna array de hasta 3 strings o null.
     */
    public static String[] parseMultiValue(String valueJson) {
        if (valueJson == null || valueJson.trim().isEmpty()) return null;
        try {
            JSONObject obj = new JSONObject(valueJson);
            if (!obj.has("values")) return null;
            JSONArray arr = obj.getJSONArray("values");
            String[] out = new String[Math.min(3, arr.length())];
            for (int i = 0; i < out.length; i++) {
                JSONObject item = arr.getJSONObject(i);
                out[i] = item.optString("value", "");
            }
            return out;
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Crea valueJson para MULTI_VALUE.
     */
    public static String toMultiValueJson(String v1, String v2, String v3) {
        try {
            JSONArray arr = new JSONArray();
            String[] vals = new String[]{v1, v2, v3};
            for (int i = 0; i < vals.length; i++) {
                String v = vals[i] != null ? vals[i].trim() : "";
                JSONObject item = new JSONObject();
                item.put("name", "value" + (i + 1));
                item.put("value", v);
                item.put("valueType", StepConstants.VALUE_TYPE_STRING);
                arr.put(item);
            }
            JSONObject obj = new JSONObject();
            obj.put("values", arr);
            return obj.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
