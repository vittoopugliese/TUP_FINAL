package com.example.tup_final.ui.steps;

import androidx.annotation.Nullable;

/**
 * Resultado de validación de un step.
 */
public class StepValidationResult {
    public final boolean valid;
    @Nullable
    public final String errorMessage;

    private StepValidationResult(boolean valid, @Nullable String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static StepValidationResult valid() {
        return new StepValidationResult(true, null);
    }

    public static StepValidationResult invalid(String message) {
        return new StepValidationResult(false, message);
    }
}
