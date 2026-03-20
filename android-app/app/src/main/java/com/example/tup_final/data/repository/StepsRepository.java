package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.StepEntity;
import com.example.tup_final.data.entity.TestEntity;
import com.example.tup_final.data.local.StepDao;
import com.example.tup_final.data.local.TestDao;
import com.example.tup_final.data.remote.StepsApi;
import com.example.tup_final.data.remote.dto.StepResponse;
import com.example.tup_final.data.remote.dto.UpdateStepRequest;
import com.example.tup_final.ui.steps.StepUiModel;
import com.example.tup_final.ui.steps.StepValidator;
import com.example.tup_final.util.Resource;
import com.example.tup_final.util.StepConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Repository para steps. Carga desde API, fallback a Room.
 */
@Singleton
public class StepsRepository {

    private final StepsApi stepsApi;
    private final StepDao stepDao;
    private final TestDao testDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public StepsRepository(StepsApi stepsApi, StepDao stepDao, TestDao testDao) {
        this.stepsApi = stepsApi;
        this.stepDao = stepDao;
        this.testDao = testDao;
    }

    /**
     * Carga steps por testId. Prioriza API, fallback a Room.
     */
    public void loadSteps(String testId, MutableLiveData<Resource<List<StepUiModel>>> result) {
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<StepResponse>> response = stepsApi.getStepsByTestId(testId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<StepUiModel> models = mapToUiModels(response.body());
                    persistToRoom(response.body(), testId);
                    mainHandler.post(() -> result.setValue(Resource.success(models)));
                } else {
                    List<StepUiModel> fromCache = loadFromRoom(testId);
                    mainHandler.post(() -> result.setValue(Resource.success(fromCache)));
                }
            } catch (Exception e) {
                try {
                    List<StepUiModel> fromCache = loadFromRoom(testId);
                    mainHandler.post(() -> result.setValue(Resource.success(fromCache)));
                } catch (Exception ex) {
                    mainHandler.post(() -> result.setValue(Resource.error(
                            e.getMessage() != null ? e.getMessage() : "Error al cargar steps")));
                }
            }
        });
    }

    /**
     * Actualiza un step y recalcula estado del test.
     */
    public void updateStep(String stepId, String valueJson, Boolean applicable,
                           MutableLiveData<Resource<StepUiModel>> result) {
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                UpdateStepRequest request = new UpdateStepRequest(valueJson, applicable);
                Response<StepResponse> response = stepsApi.updateStep(stepId, request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    StepResponse sr = response.body();
                    persistStepToRoom(sr);
                    updateTestStatusInRoom(sr.testId);
                    StepUiModel model = mapToUiModel(sr, 0);
                    mainHandler.post(() -> result.setValue(Resource.success(model)));
                } else {
                    // Fallback: actualizar solo en Room
                    StepEntity entity = stepDao.getById(stepId);
                    if (entity != null) {
                        if (valueJson != null) entity.valueJson = valueJson;
                        if (applicable != null) entity.applicable = applicable;
                        entity.status = entity.applicable
                                ? (valueJson != null && !valueJson.isEmpty()
                                        ? (StepValidator.isValueValid(valueJson, entity.testStepType, entity.minValue, entity.maxValue)
                                                ? StepConstants.STATUS_COMPLETED : StepConstants.STATUS_FAILED)
                                        : StepConstants.STATUS_PENDING)
                                : StepConstants.STATUS_COMPLETED;
                        stepDao.update(entity);
                        updateTestStatusInRoom(entity.testId);
                        StepUiModel model = mapEntityToUiModel(entity, 0);
                        mainHandler.post(() -> result.setValue(Resource.success(model)));
                    } else {
                        mainHandler.post(() -> result.setValue(Resource.error("Step no encontrado")));
                    }
                }
            } catch (Exception e) {
                // Fallback a Room
                try {
                    StepEntity entity = stepDao.getById(stepId);
                    if (entity != null) {
                        if (valueJson != null) entity.valueJson = valueJson;
                        if (applicable != null) entity.applicable = applicable;
                        entity.status = entity.applicable
                                ? (valueJson != null && !valueJson.isEmpty()
                                        ? (StepValidator.isValueValid(valueJson, entity.testStepType, entity.minValue, entity.maxValue)
                                                ? StepConstants.STATUS_COMPLETED : StepConstants.STATUS_FAILED)
                                        : StepConstants.STATUS_PENDING)
                                : StepConstants.STATUS_COMPLETED;
                        stepDao.update(entity);
                        updateTestStatusInRoom(entity.testId);
                        StepUiModel model = mapEntityToUiModel(entity, 0);
                        mainHandler.post(() -> result.setValue(Resource.success(model)));
                    } else {
                        mainHandler.post(() -> result.setValue(Resource.error(
                                e.getMessage() != null ? e.getMessage() : "Error al actualizar")));
                    }
                } catch (Exception ex) {
                    mainHandler.post(() -> result.setValue(Resource.error(
                            e.getMessage() != null ? e.getMessage() : "Error al actualizar")));
                }
            }
        });
    }

    /**
     * Marca un step como FAILED en Room de forma inmediata (offline-safe).
     * Usado cuando se guarda una observación de tipo DEFICIENCIES antes de que
     * la sincronización con la API confirme el cambio de estado.
     * También recomputa el estado del Test y emite la lista actualizada.
     */
    public void markStepFailed(String stepId,
                               MutableLiveData<Resource<List<StepUiModel>>> result) {
        executor.execute(() -> {
            try {
                StepEntity step = stepDao.getById(stepId);
                if (step == null) return;
                step.status = StepConstants.STATUS_FAILED;
                stepDao.update(step);
                updateTestStatusInRoom(step.testId);
                List<StepUiModel> updated = loadFromRoom(step.testId);
                mainHandler.post(() -> result.setValue(Resource.success(updated)));
            } catch (Exception ignored) {
                // best-effort: si falla, refreshSteps() lo corregirá luego
            }
        });
    }

    private void persistToRoom(List<StepResponse> steps, String testId) {
        for (StepResponse sr : steps) {
            persistStepToRoom(sr);
        }
    }

    private void persistStepToRoom(StepResponse sr) {
        StepEntity e = new StepEntity();
        e.id = sr.id;
        e.testId = sr.testId;
        e.name = sr.name;
        e.testStepType = sr.testStepType;
        e.applicable = sr.applicable;
        e.status = StepConstants.normalizeStepStatus(sr.status);
        e.description = sr.description;
        e.valueJson = sr.valueJson;
        e.minValue = sr.minValue;
        e.maxValue = sr.maxValue;
        e.createdAt = sr.createdAt;
        e.updatedAt = sr.updatedAt;
        stepDao.insert(e);
    }

    private void updateTestStatusInRoom(String testId) {
        TestEntity test = testDao.getById(testId);
        if (test == null) return;
        List<StepEntity> steps = stepDao.getByTestId(testId);
        String newStatus = computeTestStatus(steps);
        test.status = newStatus;
        testDao.update(test);
    }

    private String computeTestStatus(List<StepEntity> steps) {
        boolean anyFailed = false;
        boolean anyPending = false;
        for (StepEntity s : steps) {
            if (!s.applicable) continue;
            String st = StepConstants.normalizeStepStatus(s.status);
            if (StepConstants.STATUS_FAILED.equals(st)) anyFailed = true;
            if (StepConstants.STATUS_PENDING.equals(st)) anyPending = true;
        }
        if (anyFailed) return StepConstants.TEST_STATUS_FAILED;
        if (anyPending) return StepConstants.TEST_STATUS_PENDING;
        return StepConstants.TEST_STATUS_COMPLETED;
    }

    private List<StepUiModel> loadFromRoom(String testId) {
        List<StepEntity> entities = stepDao.getByTestId(testId);
        List<StepUiModel> result = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            result.add(mapEntityToUiModel(entities.get(i), i + 1));
        }
        return result;
    }

    private List<StepUiModel> mapToUiModels(List<StepResponse> responses) {
        List<StepUiModel> result = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            result.add(mapToUiModel(responses.get(i), i + 1));
        }
        return result;
    }

    private StepUiModel mapToUiModel(StepResponse sr, int index) {
        String resolvedType = StepConstants.resolveStepType(sr.testStepType, sr.minValue, sr.maxValue);
        return new StepUiModel(
                sr.id, sr.testId, sr.name, resolvedType,
                sr.applicable, StepConstants.normalizeStepStatus(sr.status), sr.description,
                sr.valueJson, sr.minValue, sr.maxValue, index
        );
    }

    private StepUiModel mapEntityToUiModel(StepEntity e, int index) {
        String resolvedType = StepConstants.resolveStepType(e.testStepType, e.minValue, e.maxValue);
        return new StepUiModel(
                e.id, e.testId, e.name, resolvedType,
                e.applicable, StepConstants.normalizeStepStatus(e.status), e.description,
                e.valueJson, e.minValue, e.maxValue, index
        );
    }
}
