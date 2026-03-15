package com.example.tup_final.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.repository.InspectionRepository;
import com.example.tup_final.util.Resource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para HomeFragment. Gestiona la lista de inspecciones y los filtros
 * (edificio, ubicación, fecha, estado).
 */
@HiltViewModel
public class HomeViewModel extends ViewModel {

    public static final String[] STATUS_OPTIONS = {
            "PENDING", "IN_PROGRESS", "DONE_FAILED", "DONE_COMPLETED"
    };

    private final InspectionRepository inspectionRepository;

    private final MutableLiveData<List<InspectionEntity>> allInspections = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<InspectionEntity>> filteredInspections = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<Resource<List<InspectionEntity>>> inspectionsResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<String>>> buildingIdsResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<String>>> locationIdsResult = new MediatorLiveData<>();

    private final MutableLiveData<String> buildingFilter = new MutableLiveData<>(null);
    private final MutableLiveData<String> locationFilter = new MutableLiveData<>(null);
    private final MutableLiveData<String> statusFilter = new MutableLiveData<>(null);
    private final MutableLiveData<Long> dateFromFilter = new MutableLiveData<>(null);
    private final MutableLiveData<Long> dateToFilter = new MutableLiveData<>(null);

    private volatile boolean isLoadingInspections = false;
    private volatile boolean pendingReloadAfterCurrentLoad = false;

    @Inject
    public HomeViewModel(InspectionRepository inspectionRepository) {
        this.inspectionRepository = inspectionRepository;
        loadInspections();
        loadBuildingIds();
        loadLocationIds();
    }

    /**
     * Carga las inspecciones desde el repositorio.
     * Si ya hay una carga en curso, programa un refresh al terminar.
     */
    public void loadInspections() {
        if (isLoadingInspections) {
            pendingReloadAfterCurrentLoad = true;
            return;
        }
        isLoadingInspections = true;
        pendingReloadAfterCurrentLoad = false;

        LiveData<Resource<List<InspectionEntity>>> source = inspectionRepository.getInspections();
        inspectionsResult.addSource(source, resource -> {
            inspectionsResult.setValue(resource);
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                allInspections.setValue(resource.getData());
                applyFilters();
                loadBuildingIds();
                loadLocationIds();
            }
            if (resource.getStatus() != Resource.Status.LOADING) {
                inspectionsResult.removeSource(source);
            }
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoadingInspections = false;
                if (pendingReloadAfterCurrentLoad) {
                    pendingReloadAfterCurrentLoad = false;
                    loadInspections();
                }
            }
        });
    }

    /**
     * Carga los IDs de edificios para el dropdown del filtro.
     */
    public void loadBuildingIds() {
        LiveData<Resource<List<String>>> source = inspectionRepository.getDistinctBuildingIds();
        buildingIdsResult.addSource(source, resource -> {
            buildingIdsResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                buildingIdsResult.removeSource(source);
            }
        });
    }

    /**
     * Carga los IDs de ubicaciones para el dropdown del filtro.
     */
    public void loadLocationIds() {
        LiveData<Resource<List<String>>> source = inspectionRepository.getDistinctLocationIds();
        locationIdsResult.addSource(source, resource -> {
            locationIdsResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                locationIdsResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<List<InspectionEntity>>> getInspectionsResult() {
        return inspectionsResult;
    }

    public LiveData<List<InspectionEntity>> getFilteredInspections() {
        return filteredInspections;
    }

    public LiveData<Resource<List<String>>> getBuildingIdsResult() {
        return buildingIdsResult;
    }

    public LiveData<Resource<List<String>>> getLocationIdsResult() {
        return locationIdsResult;
    }

    public void setBuildingFilter(String buildingId) {
        buildingFilter.setValue(buildingId != null && buildingId.trim().isEmpty() ? null : buildingId);
        applyFilters();
    }

    public void setLocationFilter(String locationId) {
        locationFilter.setValue(locationId != null && locationId.trim().isEmpty() ? null : locationId);
        applyFilters();
    }

    public void setStatusFilter(String status) {
        statusFilter.setValue(status != null && status.trim().isEmpty() ? null : status);
        applyFilters();
    }

    public void setDateFilter(Long fromMillis, Long toMillis) {
        dateFromFilter.setValue(fromMillis);
        dateToFilter.setValue(toMillis);
        applyFilters();
    }

    public void clearFilters() {
        buildingFilter.setValue(null);
        locationFilter.setValue(null);
        statusFilter.setValue(null);
        dateFromFilter.setValue(null);
        dateToFilter.setValue(null);
        applyFilters();
    }

    public void applyFilters() {
        List<InspectionEntity> all = allInspections.getValue();
        if (all == null) {
            filteredInspections.setValue(new ArrayList<>());
            return;
        }

        String building = buildingFilter.getValue();
        String location = locationFilter.getValue();
        String status = statusFilter.getValue();
        Long fromMillis = dateFromFilter.getValue();
        Long toMillis = dateToFilter.getValue();

        List<InspectionEntity> filtered = new ArrayList<>();
        for (InspectionEntity inspection : all) {
            if (!matchesBuilding(inspection, building)) continue;
            if (!matchesLocation(inspection, location)) continue;
            if (!matchesStatus(inspection, status)) continue;
            if (!matchesDateRange(inspection, fromMillis, toMillis)) continue;
            filtered.add(inspection);
        }

        Collections.sort(filtered, (a, b) -> {
            long millisA = parseToStartOfDayMillis(a.scheduledDate != null ? a.scheduledDate : "");
            long millisB = parseToStartOfDayMillis(b.scheduledDate != null ? b.scheduledDate : "");
            if (millisA == 0 && millisB == 0) return 0;
            if (millisA == 0) return 1;
            if (millisB == 0) return -1;
            return Long.compare(millisB, millisA);
        });

        filteredInspections.setValue(filtered);
    }

    private boolean matchesBuilding(InspectionEntity inspection, String building) {
        if (building == null || building.trim().isEmpty()) return true;
        return building.equals(inspection.buildingId);
    }

    private boolean matchesLocation(InspectionEntity inspection, String location) {
        if (location == null || location.trim().isEmpty()) return true;
        return location.equals(inspection.locationId);
    }

    private boolean matchesStatus(InspectionEntity inspection, String status) {
        if (status == null || status.trim().isEmpty()) return true;
        return status.equals(inspection.status);
    }

    private boolean matchesDateRange(InspectionEntity inspection, Long fromMillis, Long toMillis) {
        if (fromMillis == null && toMillis == null) return true;
        String scheduledStr = inspection.scheduledDate;
        if (scheduledStr == null || scheduledStr.isEmpty()) return false;

        try {
            long inspectionMillis = parseToStartOfDayMillis(scheduledStr);
            if (fromMillis != null && inspectionMillis < fromMillis) return false;
            if (toMillis != null && inspectionMillis > toMillis) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public LiveData<String> getBuildingFilter() {
        return buildingFilter;
    }

    public LiveData<String> getLocationFilter() {
        return locationFilter;
    }

    public LiveData<String> getStatusFilter() {
        return statusFilter;
    }

    public LiveData<Long> getDateFromFilter() {
        return dateFromFilter;
    }

    public LiveData<Long> getDateToFilter() {
        return dateToFilter;
    }

    private long parseToStartOfDayMillis(String isoOrDateStr) {
        try {
            if (isoOrDateStr.contains("T")) {
                Instant instant = Instant.parse(isoOrDateStr);
                LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else {
                LocalDate date = LocalDate.parse(isoOrDateStr.substring(0, Math.min(10, isoOrDateStr.length())),
                        DateTimeFormatter.ISO_LOCAL_DATE);
                return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
