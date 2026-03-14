package com.example.tup_final.ui.inspectionlocations;

import androidx.lifecycle.LiveData;
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

    @Inject
    public InspectionLocationsViewModel(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Obtiene las ubicaciones del edificio de la inspección.
     * Si buildingId es null o vacío, retorna todas las ubicaciones.
     */
    public LiveData<Resource<List<LocationWithStats>>> getLocationsByBuildingId(String buildingId) {
        return locationRepository.getLocationsByBuildingId(buildingId);
    }
}
