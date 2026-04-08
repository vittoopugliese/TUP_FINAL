package com.example.tup_final.data.repository;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dagger.hilt.android.qualifiers.ApplicationContext;

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
import com.example.tup_final.data.remote.dto.SignInspectionRequest;
import com.example.tup_final.util.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

import okhttp3.ResponseBody;
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
    private final Context appContext;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public InspectionRepository(InspectionApi inspectionApi, InspectionDao inspectionDao,
                                InspectionAssignmentDao assignmentDao, DeviceDao deviceDao,
                                @ApplicationContext Context appContext) {
        this.inspectionApi = inspectionApi;
        this.inspectionDao = inspectionDao;
        this.assignmentDao = assignmentDao;
        this.deviceDao = deviceDao;
        this.appContext = appContext;
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
                    List<String> freshIds = new ArrayList<>();
                    for (InspectionEntity e : entities) {
                        freshIds.add(e.id);
                        inspectionDao.upsert(e);
                    }
                    if (!freshIds.isEmpty()) {
                        inspectionDao.deleteNotIn(freshIds);
                    }
                    mainHandler.post(() -> result.setValue(Resource.success(entities)));
                } else {
                    loadFromCache(result);
                }
            } catch (Exception e) {
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
            entity.buildingName = dto.getBuildingName();
            entity.type = dto.getType();
            entity.status = dto.getStatus();
            entity.scheduledDate = dto.getScheduledDate();
            entity.approvalDate = null;
            entity.result = dto.getResult();
            entity.notes = null;
            entity.signer = dto.getSigner();
            entity.signed = dto.isSigned();
            entity.signDate = dto.getSignDate();
            entity.startedAt = null;
            entity.inspectionReportId = null;
            entity.inspectionTemplateId = null;
            entity.coverPageId = null;
            entity.createdAt = null;
            entity.updatedAt = null;
            entity.locationId = dto.getLocationId();
            entity.createdByEmail = dto.getCreatedByEmail();
            entities.add(entity);
        }
        return entities;
    }

    /**
     * Starts or continues an inspection: sets status to IN_PROGRESS
     * and records the startedAt timestamp.
     * Calls the API to notify the server, with Room-only fallback.
     */
    public LiveData<Resource<InspectionEntity>> startInspection(String inspectionId) {
        MutableLiveData<Resource<InspectionEntity>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        executor.execute(() -> {
            InspectionEntity inspection = inspectionDao.getById(inspectionId);
            if (inspection == null) {
                mainHandler.post(() -> result.setValue(
                        Resource.error("No se encontró la inspección.")));
                return;
            }

            try {
                Response<InspectionListResponse> response =
                        inspectionApi.startInspection(inspectionId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    InspectionListResponse dto = response.body();
                    inspection.status = dto.getStatus();
                }  else {
                    inspection.status = "IN_PROGRESS";
                }
            } catch (Exception ignored) {
                inspection.status = "IN_PROGRESS";
            }

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
     * Refresca el estado de una inspección consultando al backend.
     * Actualiza status y result en Room según la respuesta (signed=true tras firmar).
     * No permite regresión de status (e.g. IN_PROGRESS no se sobreescribe con PENDING).
     * Fallback cuando la API falla: devuelve la inspección actual de Room.
     */
    public LiveData<Resource<InspectionEntity>> refreshInspectionStatus(String inspectionId) {
        MutableLiveData<Resource<InspectionEntity>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<com.example.tup_final.data.remote.dto.InspectionListResponse> response =
                        inspectionApi.getInspectionStatus(inspectionId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    com.example.tup_final.data.remote.dto.InspectionListResponse dto = response.body();
                    InspectionEntity existing = inspectionDao.getById(inspectionId);
                    if (existing != null) {
                        String serverStatus = dto.getStatus();
                        if (statusPriority(serverStatus) >= statusPriority(existing.status)) {
                            existing.status = serverStatus;
                        }
                        existing.result = dto.getResult();
                        if (dto.isSigned()) {
                            existing.signed = true;
                            existing.signer = dto.getSigner();
                            existing.signDate = dto.getSignDate();
                        }
                        if (dto.getCreatedByEmail() != null && !dto.getCreatedByEmail().isEmpty()) {
                            existing.createdByEmail = dto.getCreatedByEmail();
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        existing.updatedAt = sdf.format(new Date());
                        inspectionDao.update(existing);
                        mainHandler.post(() -> result.setValue(Resource.success(existing)));
                    } else {
                        mainHandler.post(() -> result.setValue(Resource.error("Inspección no encontrada localmente")));
                    }
                } else {
                    recalculateLocalInspectionStatus(inspectionId, result);
                }
            } catch (Exception e) {
                recalculateLocalInspectionStatus(inspectionId, result);
            }
        });

        return result;
    }

    private int statusPriority(String status) {
        if (status == null) return 0;
        switch (status) {
            case "IN_PROGRESS": return 1;
            case "DONE_COMPLETED":
            case "DONE_FAILED": return 2;
            default: return 0;
        }
    }

    /**
     * Fallback cuando la API falla: devuelve la inspección actual de Room sin modificar.
     * No se recalcula status/result localmente: DONE_* solo se persiste al firmar vía API.
     */
    private void recalculateLocalInspectionStatus(String inspectionId,
                                                   MutableLiveData<Resource<InspectionEntity>> result) {
        InspectionEntity inspection = inspectionDao.getById(inspectionId);
        if (inspection == null) {
            mainHandler.post(() -> result.setValue(Resource.error("Inspección no encontrada")));
            return;
        }
        mainHandler.post(() -> result.setValue(Resource.success(inspection)));
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
            } catch (Exception e) {
                List<InspectionAssignmentEntity> cached = assignmentDao.getByInspectionId(inspectionId);
                mainHandler.post(() -> result.setValue(Resource.success(cached != null ? cached : new ArrayList<>())));
            }
        });

        return result;
    }

    /**
     * Agrega una asignacion. POST al backend y cache local.
     * La red y Room se ejecutan en executor (background), el resultado se postea al main thread.
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
            } catch (Exception e) {
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
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error de conexión")));
            }
        });

        return result;
    }

    /**
     * Descarga el reporte PDF de la inspección y lo guarda localmente.
     * Requiere inspección firmada y usuario asignado como inspector u operador.
     */
    public LiveData<Resource<File>> downloadInspectionReport(String inspectionId) {
        MutableLiveData<Resource<File>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<ResponseBody> response = inspectionApi.getInspectionReportPdf(inspectionId).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    String err = response.errorBody() != null ? response.errorBody().string() : "Error al descargar";
                    mainHandler.post(() -> result.setValue(Resource.error(err != null ? err : "Error al descargar reporte")));
                    return;
                }
                File dir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                if (dir == null) dir = appContext.getCacheDir();
                File pdfFile = new File(dir, "inspection-" + inspectionId + ".pdf");
                try (InputStream in = response.body().byteStream();
                     FileOutputStream out = new FileOutputStream(pdfFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                mainHandler.post(() -> result.setValue(Resource.success(pdfFile)));
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Error al descargar el reporte";
                mainHandler.post(() -> result.setValue(Resource.error(msg)));
            }
        });

        return result;
    }

    /**
     * Firma una inspección: envía el nombre del firmante al backend y
     * actualiza la entidad local con el resultado (status DONE_*, signed, signer, signDate).
     * Si la red falla, se aplica la firma localmente en Room como fallback offline.
     */
    public LiveData<Resource<InspectionEntity>> signInspection(String inspectionId, String signerName) {
        MutableLiveData<Resource<InspectionEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                SignInspectionRequest req = new SignInspectionRequest(signerName);
                Response<InspectionListResponse> response =
                        inspectionApi.signInspection(inspectionId, req).execute();

                if (response.isSuccessful() && response.body() != null) {
                    InspectionListResponse dto = response.body();
                    InspectionEntity entity = inspectionDao.getById(inspectionId);
                    if (entity != null) {
                        entity.status   = dto.getStatus();
                        entity.signer   = dto.getSigner();
                        entity.signed   = dto.isSigned();
                        entity.signDate = dto.getSignDate();
                        if (dto.getCreatedByEmail() != null && !dto.getCreatedByEmail().isEmpty()) {
                            entity.createdByEmail = dto.getCreatedByEmail();
                        }
                        entity.updatedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                                .format(new Date());
                        inspectionDao.update(entity);
                        mainHandler.post(() -> result.setValue(Resource.success(entity)));
                    } else {
                        mainHandler.post(() -> result.setValue(
                                Resource.error("No se encontró la inspección local")));
                    }
                } else {
                    String errorMsg = "Error al firmar la inspección";
                    if (response.errorBody() != null) {
                        try {
                            String body = response.errorBody().string();
                            if (body.contains("message")) {
                                int idx = body.indexOf("message");
                                int start = body.indexOf("\"", idx + 9) + 1;
                                int end = body.indexOf("\"", start);
                                if (start > 0 && end > start) {
                                    errorMsg = body.substring(start, end);
                                }
                            } else if (!body.isEmpty()) {
                                errorMsg = body;
                            }
                        } catch (Exception ignored) {}
                    }
                    final String msg = errorMsg;
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Error de conexión al firmar";
                mainHandler.post(() -> result.setValue(Resource.error(msg)));
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
