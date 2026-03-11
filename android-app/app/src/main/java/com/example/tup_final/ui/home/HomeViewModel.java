package com.example.tup_final.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.repository.InspectionRepository;
import com.example.tup_final.util.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla Home.
 * Carga la lista de inspecciones desde el InspectionRepository.
 */
@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final InspectionRepository inspectionRepository;
    private LiveData<Resource<List<InspectionEntity>>> inspections;

    @Inject
    public HomeViewModel(InspectionRepository inspectionRepository) {
        this.inspectionRepository = inspectionRepository;
    }

    /**
     * Retorna LiveData con la lista de inspecciones.
     * Carga automáticamente la primera vez.
     */
    public LiveData<Resource<List<InspectionEntity>>> getInspections() {
        if (inspections == null) {
            loadInspections();
        }
        return inspections;
    }

    /**
     * Recarga las inspecciones desde el repository.
     */
    public void loadInspections() {
        inspections = inspectionRepository.getInspections();
    }
}
