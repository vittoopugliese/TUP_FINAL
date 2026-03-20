package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request body para firmar una inspección.
 * POST /api/inspections/{id}/sign
 */
public class SignInspectionRequest {

    @SerializedName("signerName")
    public String signerName;

    public SignInspectionRequest() {}

    public SignInspectionRequest(String signerName) {
        this.signerName = signerName;
    }
}
