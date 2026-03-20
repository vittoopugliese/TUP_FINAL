package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO Retrofit para un tipo de deficiencia del catálogo del backend.
 */
public class DeficiencyTypeResponse {

    @SerializedName("id")          public String id;
    @SerializedName("code")        public String code;
    @SerializedName("name")        public String name;
    @SerializedName("description") public String description;
    @SerializedName("category")    public String category;
    @SerializedName("enabled")     public boolean enabled;
}
