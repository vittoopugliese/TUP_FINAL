package com.example.tup_final.ui.inspectiontests;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo UI para una zona con sus devices y tests.
 */
public class ZoneUiModel {
    public final String id;
    public final String locationId;
    public final String name;
    public final String details;
    public final List<DeviceUiModel> devices;

    public ZoneUiModel(String id, String locationId, String name, String details,
                      List<DeviceUiModel> devices) {
        this.id = id;
        this.locationId = locationId;
        this.name = name != null ? name : "";
        this.details = details;
        this.devices = devices != null ? devices : new ArrayList<>();
    }
}
