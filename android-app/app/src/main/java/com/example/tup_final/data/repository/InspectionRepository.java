package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.DeviceEntity;
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.local.DeviceDao;
import com.example.tup_final.data.local.InspectionDao;
import com.example.tup_final.util.Resource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InspectionRepository {

    private final InspectionDao inspectionDao;
    private final DeviceDao deviceDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public InspectionRepository(InspectionDao inspectionDao, DeviceDao deviceDao) {
        this.inspectionDao = inspectionDao;
        this.deviceDao = deviceDao;
    }

    public LiveData<Resource<InspectionEntity>> getInspectionById(String inspectionId) {
        MutableLiveData<Resource<InspectionEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            InspectionEntity inspection = inspectionDao.getById(inspectionId);
            if (inspection != null) {
                mainHandler.post(() -> result.setValue(Resource.success(inspection)));
            } else {
                mainHandler.post(() -> result.setValue(
                        Resource.error("No se encontró la inspección.")));
            }
        });

        return result;
    }

    public LiveData<Resource<List<InspectionEntity>>> getAllInspections() {
        MutableLiveData<Resource<List<InspectionEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            List<InspectionEntity> inspections = inspectionDao.getAll();
            mainHandler.post(() -> result.setValue(Resource.success(inspections)));
        });

        return result;
    }

    public LiveData<Resource<List<DeviceEntity>>> getDevicesByBuildingId(String buildingId) {
        MutableLiveData<Resource<List<DeviceEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            List<DeviceEntity> devices = deviceDao.getByBuildingId(buildingId);
            mainHandler.post(() -> result.setValue(Resource.success(devices)));
        });

        return result;
    private List<InspectionEntity> mapToEntities(List<InspectionListResponse> dtos) {
        List<InspectionEntity> entities = new ArrayList<>();
        for (InspectionListResponse dto : dtos) {
            InspectionEntity entity = new InspectionEntity();
            entity.id = dto.getId() != null ? dto.getId() : "";
            entity.buildingId = dto.getBuildingId();
            entity.type = dto.getType();
            entity.status = dto.getStatus();
            entity.scheduledDate = dto.getScheduledDate();
            // Campos opcionales no presentes en el DTO mínimo - se dejan null
            entity.approvalDate = null;
            entity.result = null;
            entity.notes = null;
            entity.signer = null;
            entity.signed = false;
            entity.signDate = null;
            entity.startedAt = null;
            entity.inspectionReportId = null;
            entity.inspectionTemplateId = null;
            entity.coverPageId = null;
            entity.createdAt = null;
            entity.updatedAt = null;
            entity.locationId = null;
            entities.add(entity);
        }
        return entities;
    }

    /**
     * Starts or continues an inspection: sets status to IN_PROGRESS
     * and records the startedAt timestamp.
     */
    public LiveData<Resource<InspectionEntity>> startInspection(String inspectionId) {
        MutableLiveData<Resource<InspectionEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            InspectionEntity inspection = inspectionDao.getById(inspectionId);
            if (inspection == null) {
                mainHandler.post(() -> result.setValue(
                        Resource.error("No se encontró la inspección.")));
                return;
            }

            inspection.status = "IN_PROGRESS";
            if (inspection.startedAt == null || inspection.startedAt.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                inspection.startedAt = sdf.format(new Date());
            }
            inspection.updatedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    .format(new Date());

            inspectionDao.update(inspection);
            mainHandler.post(() -> result.setValue(Resource.success(inspection)));
        });

        return result;
    }

    /**
     * Obtiene los IDs de edificios únicos para poblar el filtro.
     *
     * @return LiveData con Resource que contiene la lista de buildingIds
     */
    public LiveData<Resource<List<String>>> getDistinctBuildingIds() {
        MutableLiveData<Resource<List<String>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                List<String> buildingIds = inspectionDao.getDistinctBuildingIds();
                mainHandler.post(() -> result.setValue(Resource.success(buildingIds)));
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error al cargar edificios")));
            }
        });

        return result;
    }

    /**
     * Obtiene los IDs de ubicaciones únicos para poblar el filtro.
     *
     * @return LiveData con Resource que contiene la lista de locationIds
     */
    public LiveData<Resource<List<String>>> getDistinctLocationIds() {
        MutableLiveData<Resource<List<String>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                List<String> locationIds = inspectionDao.getDistinctLocationIds();
                mainHandler.post(() -> result.setValue(Resource.success(locationIds)));
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error al cargar ubicaciones")));
            }
        });

        return result;
    }
}