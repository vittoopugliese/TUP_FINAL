package com.example.tup_final.ui.inspectionlocations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.LocationWithStats;
import com.example.tup_final.data.repository.LocationRepository;
import com.example.tup_final.util.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de ubicaciones de una inspección.
 * Carga las locations filtradas por buildingId de la inspección.
 */
@HiltViewModel
public class InspectionLocationsViewModel extends ViewModel {

    private final LocationRepository locationRepository;
    private final MutableLiveData<Resource<List<LocationWithStats>>> locationsData = new MutableLiveData<>();

    @Inject
    public InspectionLocationsViewModel(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void loadLocations(String buildingId) {
        locationRepository.loadLocationsByBuildingId(buildingId, locationsData);
    }

    public LiveData<Resource<List<LocationWithStats>>> getLocations() {
        return locationsData;
    }
}
