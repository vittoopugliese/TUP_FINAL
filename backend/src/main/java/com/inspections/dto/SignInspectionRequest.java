package com.inspections.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body para firmar una inspección.
 * El inspector envía su nombre completo como firmante.
 */
public class SignInspectionRequest {

    @NotBlank(message = "El nombre del firmante es obligatorio")
    private String signerName;

    public SignInspectionRequest() {}

    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
}
