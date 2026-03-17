package com.example.tup_final.ui.inspectiontests;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.remote.dto.CreateDeviceRequest;
import com.example.tup_final.data.remote.dto.CreateZoneRequest;
import com.example.tup_final.data.remote.dto.DeviceTypeResponse;
import com.example.tup_final.data.remote.dto.MoveDeviceRequest;
import com.example.tup_final.data.remote.dto.MoveDeviceResponse;
import com.example.tup_final.data.repository.InspectionTestsRepository;
import com.example.tup_final.util.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class InspectionTestsViewModel extends ViewModel {

    private final InspectionTestsRepository repository;

    private final MutableLiveData<Resource<List<ZoneUiModel>>> zonesData = new MutableLiveData<>();
    private final MutableLiveData<Resource<ZoneUiModel>> createZoneResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<DeviceUiModel>> createDeviceResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<MoveDeviceResponse>> moveDeviceResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<DeviceTypeResponse>>> deviceTypesResult = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> expandedZoneIds = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Set<String>> expandedDeviceIds = new MutableLiveData<>(new HashSet<>());

    private String lastLocationId;
    private String lastInspectionId;

    @Inject
    public InspectionTestsViewModel(InspectionTestsRepository repository) {
        this.repository = repository;
    }

    public void loadZones(String locationId, String inspectionId) {
        this.lastLocationId = locationId;
        this.lastInspectionId = inspectionId;
        repository.loadZonesWithDevicesAndTests(locationId, inspectionId, zonesData);
    }

    public void createZone(String locationId, CreateZoneRequest request) {
        repository.createZone(locationId, request, createZoneResult);
    }

    public void createDevice(String locationId, String zoneId, CreateDeviceRequest request) {
        repository.createDevice(locationId, zoneId, request, createDeviceResult);
    }

    public void moveDevice(String locationId, String deviceId, MoveDeviceRequest request) {
        repository.moveDevice(locationId, deviceId, request, moveDeviceResult);
    }

    public void loadDeviceTypes() {
        repository.loadDeviceTypes(deviceTypesResult);
    }

    public LiveData<Resource<List<DeviceTypeResponse>>> getDeviceTypes() {
        return deviceTypesResult;
    }

    public LiveData<Resource<MoveDeviceResponse>> getMoveDeviceResult() {
        return moveDeviceResult;
    }

    public void clearMoveDeviceResult() {
        moveDeviceResult.setValue(null);
    }

    public LiveData<Resource<DeviceUiModel>> getCreateDeviceResult() {
        return createDeviceResult;
    }

    public void clearCreateDeviceResult() {
        createDeviceResult.setValue(null);
    }

    public LiveData<Resource<ZoneUiModel>> getCreateZoneResult() {
        return createZoneResult;
    }

    public void clearCreateZoneResult() {
        createZoneResult.setValue(null);
    }

    public String getLastLocationId() {
        return lastLocationId;
    }

    public String getLastInspectionId() {
        return lastInspectionId;
    }

    public LiveData<Resource<List<ZoneUiModel>>> getZones() {
        return zonesData;
    }

    public LiveData<Set<String>> getExpandedZoneIds() {
        return expandedZoneIds;
    }

    public LiveData<Set<String>> getExpandedDeviceIds() {
        return expandedDeviceIds;
    }

    public void toggleZoneExpanded(String zoneId) {
        Set<String> current = expandedZoneIds.getValue();
        if (current == null) current = new HashSet<>();
        Set<String> next = new HashSet<>(current);
        if (next.contains(zoneId)) {
            next.remove(zoneId);
        } else {
            next.add(zoneId);
        }
        expandedZoneIds.setValue(next);
    }

    public void ensureZoneExpanded(String zoneId) {
        Set<String> current = expandedZoneIds.getValue();
        if (current == null) current = new HashSet<>();
        if (current.contains(zoneId)) return;
        Set<String> next = new HashSet<>(current);
        next.add(zoneId);
        expandedZoneIds.setValue(next);
    }

    public void ensureDeviceExpanded(String deviceId) {
        Set<String> current = expandedDeviceIds.getValue();
        if (current == null) current = new HashSet<>();
        if (current.contains(deviceId)) return;
        Set<String> next = new HashSet<>(current);
        next.add(deviceId);
        expandedDeviceIds.setValue(next);
    }

    public void toggleDeviceExpanded(String deviceId) {
        Set<String> current = expandedDeviceIds.getValue();
        if (current == null) current = new HashSet<>();
        Set<String> next = new HashSet<>(current);
        if (next.contains(deviceId)) {
            next.remove(deviceId);
        } else {
            next.add(deviceId);
        }
        expandedDeviceIds.setValue(next);
    }

    public boolean isZoneExpanded(String zoneId) {
        Set<String> set = expandedZoneIds.getValue();
        return set != null && set.contains(zoneId);
    }

    public boolean isDeviceExpanded(String deviceId) {
        Set<String> set = expandedDeviceIds.getValue();
        return set != null && set.contains(deviceId);
    }
}
