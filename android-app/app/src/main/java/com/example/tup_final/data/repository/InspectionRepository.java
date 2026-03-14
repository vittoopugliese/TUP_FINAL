package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.DeviceEntity;
import com.example.tup_final.data.entity.InspectionAssignmentEntity;
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.local.DeviceDao;
import com.example.tup_final.data.local.InspectionAssignmentDao;
import com.example.tup_final.data.local.InspectionDao;
import com.example.tup_final.data.remote.InspectionApi;
import com.example.tup_final.data.remote.dto.AssignmentRequest;
import com.example.tup_final.data.remote.dto.AssignmentResponse;
import com.example.tup_final.data.remote.dto.InspectionListResponse;
import com.example.tup_final.util.Resource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Repository para inspecciones.
 * Patrón offline-first: intenta online, cachea en Room, fallback a Room.
 */
@Singleton
public class InspectionRepository {

    private final InspectionApi inspectionApi;
    private final InspectionDao inspectionDao;
    private final InspectionAssignmentDao assignmentDao;
    private final DeviceDao deviceDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public InspectionRepository(InspectionApi inspectionApi, InspectionDao inspectionDao,
                                InspectionAssignmentDao assignmentDao, DeviceDao deviceDao) {
        this.inspectionApi = inspectionApi;
        this.inspectionDao = inspectionDao;
        this.assignmentDao = assignmentDao;
        this.deviceDao = deviceDao;
    }

    /**
     * Obtiene las inspecciones asignadas al usuario.
     * Online: llama al backend, guarda en Room y retorna.
     * Offline: lee de Room como fallback.
     *
     * @return LiveData con el estado del recurso
     */
    public LiveData<Resource<List<InspectionEntity>>> getInspections() {
        MutableLiveData<Resource<List<InspectionEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<InspectionListResponse>> response = inspectionApi.getInspections().execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<InspectionEntity> entities = mapToEntities(response.body());
                    inspectionDao.deleteAll();
                    if (!entities.isEmpty()) {
                        inspectionDao.insertAll(entities);
                    }
                    mainHandler.post(() -> result.setValue(Resource.success(entities)));
                } else {
                    loadFromCache(result);
                }
            } catch (IOException e) {
                loadFromCache(result);
            }
        });

        return result;
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
    }

    private void loadFromCache(MutableLiveData<Resource<List<InspectionEntity>>> result) {
        List<InspectionEntity> cached = inspectionDao.getAll();
        if (cached != null && !cached.isEmpty()) {
            mainHandler.post(() -> result.setValue(Resource.success(cached)));
        } else {
            mainHandler.post(() -> result.setValue(
                    Resource.error("No se pudieron cargar las inspecciones. Verificá tu conexión.")));
        }
    }

    private List<InspectionEntity> mapToEntities(List<InspectionListResponse> dtos) {
        List<InspectionEntity> entities = new ArrayList<>();
        for (InspectionListResponse dto : dtos) {
            InspectionEntity entity = new InspectionEntity();
            entity.id = dto.getId() != null ? dto.getId() : "";
            entity.buildingId = dto.getBuildingId();
            entity.type = dto.getType();
            entity.status = dto.getStatus();
            entity.scheduledDate = dto.getScheduledDate();
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
            entity.locationId = dto.getLocationId();
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
     * Obtiene las asignaciones de una inspeccion.
     * Offline-first: intenta API, cachea en Room, fallback a Room.
     */
    public LiveData<Resource<List<InspectionAssignmentEntity>>> getAssignments(String inspectionId) {
        MutableLiveData<Resource<List<InspectionAssignmentEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<AssignmentResponse>> response = inspectionApi.getAssignments(inspectionId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<InspectionAssignmentEntity> entities = mapAssignmentsToEntities(inspectionId, response.body());
                    assignmentDao.deleteByInspectionId(inspectionId);
                    if (!entities.isEmpty()) {
                        assignmentDao.insertAll(entities);
                    }
                    mainHandler.post(() -> result.setValue(Resource.success(entities)));
                } else {
                    List<InspectionAssignmentEntity> cached = assignmentDao.getByInspectionId(inspectionId);
                    mainHandler.post(() -> result.setValue(Resource.success(cached != null ? cached : new ArrayList<>())));
                }
            } catch (IOException e) {
                List<InspectionAssignmentEntity> cached = assignmentDao.getByInspectionId(inspectionId);
                mainHandler.post(() -> result.setValue(Resource.success(cached != null ? cached : new ArrayList<>())));
            }
        });

        return result;
    }

    /**
     * Agrega una asignacion. POST al backend y cache local.
     */
    public LiveData<Resource<AssignmentResponse>> addAssignment(String inspectionId, String userEmail, String role) {
        MutableLiveData<Resource<AssignmentResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                AssignmentRequest request = new AssignmentRequest(userEmail, role);
                Response<AssignmentResponse> response = inspectionApi.addAssignment(inspectionId, request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    InspectionAssignmentEntity entity = mapAssignmentToEntity(response.body());
                    assignmentDao.insert(entity);
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error al agregar";
                    mainHandler.post(() -> result.setValue(Resource.error(errorBody)));
                }
            } catch (IOException e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error de conexión")));
            }
        });

        return result;
    }

    /**
     * Remueve una asignacion. DELETE al backend y remove local.
     */
    public LiveData<Resource<Void>> removeAssignment(String inspectionId, String userEmail) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<Void> response = inspectionApi.removeAssignment(inspectionId, userEmail).execute();
                if (response.isSuccessful()) {
                    assignmentDao.deleteByInspectionIdAndEmail(inspectionId, userEmail);
                    mainHandler.post(() -> result.setValue(Resource.success(null)));
                } else {
                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error al remover";
                    mainHandler.post(() -> result.setValue(Resource.error(errorBody)));
                }
            } catch (IOException e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error de conexión")));
            }
        });

        return result;
    }

    private List<InspectionAssignmentEntity> mapAssignmentsToEntities(String inspectionId, List<AssignmentResponse> dtos) {
        List<InspectionAssignmentEntity> entities = new ArrayList<>();
        for (AssignmentResponse dto : dtos) {
            InspectionAssignmentEntity entity = new InspectionAssignmentEntity();
            entity.id = dto.getId() != null ? dto.getId() : "";
            entity.inspectionId = inspectionId;
            entity.userEmail = dto.getUserEmail();
            entity.role = dto.getRole();
            entity.createdAt = dto.getCreatedAt();
            entities.add(entity);
        }
        return entities;
    }

    private InspectionAssignmentEntity mapAssignmentToEntity(AssignmentResponse dto) {
        InspectionAssignmentEntity entity = new InspectionAssignmentEntity();
        entity.id = dto.getId() != null ? dto.getId() : "";
        entity.inspectionId = dto.getInspectionId();
        entity.userEmail = dto.getUserEmail();
        entity.role = dto.getRole();
        entity.createdAt = dto.getCreatedAt();
        return entity;
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
