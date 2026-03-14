package com.example.tup_final.ui.inspectiontests;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    private LiveData<Resource<List<ZoneUiModel>>> zonesData;
    private final MutableLiveData<Set<String>> expandedZoneIds = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Set<String>> expandedDeviceIds = new MutableLiveData<>(new HashSet<>());

    @Inject
    public InspectionTestsViewModel(InspectionTestsRepository repository) {
        this.repository = repository;
    }

    public void loadZones(String locationId, String inspectionId) {
        zonesData = repository.getZonesWithDevicesAndTests(locationId, inspectionId);
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
