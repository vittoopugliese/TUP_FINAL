package com.example.tup_final.ui.inspectiontests;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo UI para un device con sus tests.
 */
public class DeviceUiModel {
    public final String id;
    public final String zoneId;
    public final String locationId;
    public final String name;
    public final String deviceCategory;
    public final Integer deviceSerialNumber;
    public final boolean enabled;
    public final List<TestUiModel> tests;

    public DeviceUiModel(String id, String zoneId, String locationId, String name,
                        String deviceCategory, Integer deviceSerialNumber, boolean enabled,
                        List<TestUiModel> tests) {
        this.id = id;
        this.zoneId = zoneId;
        this.locationId = locationId;
        this.name = name != null ? name : "";
        this.deviceCategory = deviceCategory;
        this.deviceSerialNumber = deviceSerialNumber;
        this.enabled = enabled;
        this.tests = tests != null ? tests : new ArrayList<>();
    }
}
