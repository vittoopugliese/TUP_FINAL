package com.example.tup_final.ui.locations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.LocationEntity;
import com.example.tup_final.data.repository.LocationRepository;
import com.example.tup_final.util.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de crear ubicación (T4.1.2).
 * Valida nombre no vacío. La validación de duplicados la hace el repository.
 */
@HiltViewModel
public class CreateLocationViewModel extends ViewModel {

    private final LocationRepository locationRepository;

    private final MediatorLiveData<Resource<LocationEntity>> createResult = new MediatorLiveData<>();
    private final MutableLiveData<String> nameError = new MutableLiveData<>();

    @Inject
    public CreateLocationViewModel(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Resultado observable del intento de crear ubicación.
     */
    public LiveData<Resource<LocationEntity>> getCreateResult() {
        return createResult;
    }

    /**
     * LiveData para error de nombre (vacío).
     */
    public LiveData<String> getNameError() {
        return nameError;
    }

    /**
     * Crea una nueva ubicación. Valida nombre no vacío antes de llamar al repository.
     */
    public void createLocation(String name, String details) {
        String trimmedName = name != null ? name.trim() : "";

        if (trimmedName.isEmpty()) {
            nameError.setValue("error_name_required");
            return;
        }
        nameError.setValue(null);

        createResult.setValue(Resource.loading());

        LiveData<Resource<LocationEntity>> source = locationRepository.createLocation(trimmedName, details);
        createResult.addSource(source, resource -> {
            createResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                createResult.removeSource(source);
            }
        });
    }
}
