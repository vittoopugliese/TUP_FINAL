package com.example.tup_final.ui.steps;

import androidx.annotation.Nullable;

/**
 * Modelo de UI para un Step.
 */
public class StepUiModel {
    public final String id;
    public final String testId;
    public final String name;
    /** Tipo resuelto: BINARY, DATE_RANGE, SIMPLE_VALUE, NUMERIC_RANGE, MULTI_VALUE. */
    public final String testStepType;
    public final boolean applicable;
    public final String status;
    public final String description;
    @Nullable
    public final String valueJson;
    @Nullable
    public final Double minValue;
    @Nullable
    public final Double maxValue;
    public final int index;

    public StepUiModel(String id, String testId, String name, String testStepType,
                       boolean applicable, String status, String description,
                       @Nullable String valueJson, @Nullable Double minValue, @Nullable Double maxValue,
                       int index) {
        this.id = id;
        this.testId = testId;
        this.name = name;
        this.testStepType = testStepType;
        this.applicable = applicable;
        this.status = status;
        this.description = description;
        this.valueJson = valueJson;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.index = index;
    }
}
