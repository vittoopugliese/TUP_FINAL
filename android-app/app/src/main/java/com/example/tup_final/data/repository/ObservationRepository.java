package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.ObservationEntity;
import com.example.tup_final.data.local.ObservationDao;
import com.example.tup_final.data.remote.ObservationApi;
import com.example.tup_final.data.remote.dto.CreateObservationRequest;
import com.example.tup_final.data.remote.dto.ObservationResponse;
import com.example.tup_final.util.Resource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Repositorio de observaciones. Estrategia local-first:
 * 1. Guarda en Room inmediatamente (UUID generado en cliente).
 * 2. Intenta sincronizar con la API en background.
 */
@Singleton
public class ObservationRepository {

    private final ObservationDao observationDao;
    private final ObservationApi observationApi;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public ObservationRepository(ObservationDao observationDao, ObservationApi observationApi) {
        this.observationDao = observationDao;
        this.observationApi = observationApi;
    }

    /**
     * Guarda la observación localmente y luego intenta sincronizar con la API.
     *
     * @param stepId       ID del step al que se adjunta.
     * @param inspectionId ID de la inspección (para indexar).
     * @param type         "REMARKS" (Observación) o "DEFICIENCIES" (Deficiencia).
     * @param description  Texto obligatorio.
     * @param photoPath    Ruta local de la foto (null si no aplica).
     * @param result       LiveData al que se publica el resultado.
     */
    public void saveObservation(String stepId, String inspectionId,
                                String type, String description, String photoPath,
                                MutableLiveData<Resource<ObservationEntity>> result) {
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                String id = UUID.randomUUID().toString();
                String now = Instant.now().toString();

                ObservationEntity entity = new ObservationEntity();
                entity.id = id;
                entity.testStepId = stepId;
                entity.inspectionId = inspectionId;
                entity.type = type;
                entity.description = description;
                entity.mediaId = photoPath;
                entity.name = "DEFICIENCIES".equals(type) ? "Deficiencia" : "Observación";
                entity.createdAt = now;
                entity.updatedAt = now;

                observationDao.insert(entity);

                mainHandler.post(() -> result.setValue(Resource.success(entity)));

                trySyncToApi(entity);

            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error al guardar observación")));
            }
        });
    }

    /**
     * Carga las observaciones de un step desde Room.
     */
    public List<ObservationEntity> getObservationsForStep(String stepId) {
        return observationDao.getByStepId(stepId);
    }

    /**
     * Carga y refresca las observaciones desde la API, guardando en Room.
     */
    public void loadObservationsForStep(String stepId,
                                        MutableLiveData<Resource<List<ObservationEntity>>> result) {
        result.setValue(Resource.loading());
        executor.execute(() -> {
            try {
                Response<List<ObservationResponse>> response =
                        observationApi.getObservationsByStep(stepId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<ObservationEntity> entities = new ArrayList<>();
                    for (ObservationResponse dto : response.body()) {
                        ObservationEntity e = toEntity(dto);
                        observationDao.insert(e);
                        entities.add(e);
                    }
                    mainHandler.post(() -> result.setValue(Resource.success(entities)));
                } else {
                    List<ObservationEntity> cached = observationDao.getByStepId(stepId);
                    mainHandler.post(() -> result.setValue(Resource.success(cached)));
                }
            } catch (Exception e) {
                List<ObservationEntity> cached = observationDao.getByStepId(stepId);
                mainHandler.post(() -> result.setValue(Resource.success(cached)));
            }
        });
    }

    private void trySyncToApi(ObservationEntity entity) {
        try {
            CreateObservationRequest req = new CreateObservationRequest(
                    entity.type, entity.description, entity.inspectionId, entity.mediaId);
            Response<ObservationResponse> response =
                    observationApi.createObservation(entity.testStepId, req).execute();
            if (response.isSuccessful() && response.body() != null) {
                ObservationEntity synced = toEntity(response.body());
                observationDao.insert(synced);
            }
        } catch (Exception ignored) {
            // Se sincronizará en la próxima oportunidad (SyncWorker)
        }
    }

    private ObservationEntity toEntity(ObservationResponse dto) {
        ObservationEntity e = new ObservationEntity();
        e.id = dto.id;
        e.testStepId = dto.testStepId;
        e.inspectionId = dto.inspectionId;
        e.name = dto.name;
        e.type = dto.type;
        e.description = dto.description;
        e.deficiencyTypeId = dto.deficiencyTypeId;
        e.mediaId = dto.mediaId;
        e.createdAt = dto.createdAt;
        e.updatedAt = dto.updatedAt;
        return e;
    }
}
