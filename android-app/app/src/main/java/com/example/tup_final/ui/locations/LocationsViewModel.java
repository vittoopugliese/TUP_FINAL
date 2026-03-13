package com.example.tup_final.ui.locations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.LocationWithStats;
import com.example.tup_final.data.repository.LocationRepository;
import com.example.tup_final.util.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de lista de ubicaciones (T4.1.1).
 */
@HiltViewModel
public class LocationsViewModel extends ViewModel {

    private final LiveData<Resource<List<LocationWithStats>>> locationsResult;

    @Inject
    public LocationsViewModel(LocationRepository locationRepository) {
        this.locationsResult = locationRepository.getAllLocations();
    }

    /**
     * Resultado observable de la carga de ubicaciones.
     */
    public LiveData<Resource<List<LocationWithStats>>> getLocationsResult() {
        return locationsResult;
    }
}
