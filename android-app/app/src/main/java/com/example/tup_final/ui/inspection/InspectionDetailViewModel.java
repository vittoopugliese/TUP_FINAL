package com.example.tup_final.ui.inspection;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.DeviceEntity;
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.local.UserDao;
import com.example.tup_final.data.repository.InspectionRepository;
import com.example.tup_final.util.Resource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class InspectionDetailViewModel extends ViewModel {

    private final InspectionRepository inspectionRepository;
    private final UserDao userDao;
    private final SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private MutableLiveData<Resource<InspectionEntity>> inspection;
    private MutableLiveData<Resource<List<DeviceEntity>>> devices;
    private final MediatorLiveData<Resource<InspectionEntity>> startResult = new MediatorLiveData<>();

    private String currentInspectionId;

    @Inject
    public InspectionDetailViewModel(InspectionRepository inspectionRepository,
                                     UserDao userDao,
                                     SharedPreferences prefs) {
        this.inspectionRepository = inspectionRepository;
        this.userDao = userDao;
        this.prefs = prefs;
    }

    public void loadInspection(String inspectionId) {
        this.currentInspectionId = inspectionId;
        inspection = (MutableLiveData<Resource<InspectionEntity>>)
                inspectionRepository.getInspectionById(inspectionId);
    }

    public LiveData<Resource<InspectionEntity>> getInspection() {
        return inspection;
    }

    public void loadDevices(String buildingId) {
        devices = (MutableLiveData<Resource<List<DeviceEntity>>>)
                inspectionRepository.getDevicesByBuildingId(buildingId);
    }

    public LiveData<Resource<List<DeviceEntity>>> getDevices() {
        return devices;
    }

    public LiveData<Resource<InspectionEntity>> getStartResult() {
        return startResult;
    }

    /**
     * Validates that the current user has INSPECTOR role,
     * then transitions the inspection to IN_PROGRESS.
     */
    public void startOrContinueInspection() {
        if (currentInspectionId == null) {
            startResult.setValue(Resource.error("No se encontró la inspección."));
            return;
        }

        startResult.setValue(Resource.loading());

        executor.execute(() -> {
            String userId = prefs.getString("cached_user_id", null);
            if (userId == null || userId.isEmpty()) {
                startResult.postValue(Resource.error("No se encontró el usuario. Iniciá sesión nuevamente."));
                return;
            }

            UserEntity user = userDao.getById(userId);
            if (user == null || !"INSPECTOR".equalsIgnoreCase(user.role)) {
                startResult.postValue(
                        Resource.error("Se requiere al menos 1 Inspector asignado para iniciar la inspección."));
                return;
            }

            LiveData<Resource<InspectionEntity>> source =
                    inspectionRepository.startInspection(currentInspectionId);

            startResult.postValue(Resource.loading());
            // Observe on main thread via MediatorLiveData
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                    startResult.addSource(source, resource -> {
                        startResult.setValue(resource);
                        if (resource.getStatus() != Resource.Status.LOADING) {
                            startResult.removeSource(source);
                        }
                    })
            );
        });
    }

    public boolean isInspectionStarted() {
        if (inspection != null && inspection.getValue() != null
                && inspection.getValue().getData() != null) {
            String status = inspection.getValue().getData().status;
            return "IN_PROGRESS".equals(status);
        }
        return false;
    }

    public String getCurrentInspectionId() {
        return currentInspectionId;
    }
}
