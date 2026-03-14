package com.example.tup_final.ui.inspectiontests;

/**
 * Modelo UI para un test.
 */
public class TestUiModel {
    public final String id;
    public final String deviceId;
    public final String inspectionId;
    public final String name;
    public final String description;
    public final String status;

    public TestUiModel(String id, String deviceId, String inspectionId, String name,
                       String description, String status) {
        this.id = id;
        this.deviceId = deviceId;
        this.inspectionId = inspectionId;
        this.name = name != null ? name : "";
        this.description = description;
        this.status = status;
    }
}
