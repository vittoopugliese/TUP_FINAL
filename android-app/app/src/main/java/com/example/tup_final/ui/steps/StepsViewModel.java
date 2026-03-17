package com.example.tup_final.ui.steps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.ObservationEntity;
import com.example.tup_final.data.repository.ObservationRepository;
import com.example.tup_final.data.repository.StepsRepository;
import com.example.tup_final.util.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StepsViewModel extends ViewModel {

    private final StepsRepository repository;
    private final ObservationRepository observationRepository;

    private final MutableLiveData<Resource<List<StepUiModel>>> stepsData = new MutableLiveData<>();
    private final MutableLiveData<Resource<StepUiModel>> updateStepResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<ObservationEntity>> saveObsResult = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<ObservationEntity>>> observationsByStep =
            new MutableLiveData<>(new HashMap<>());

    private String currentTestId;

    @Inject
    public StepsViewModel(StepsRepository repository, ObservationRepository observationRepository) {
        this.repository = repository;
        this.observationRepository = observationRepository;
    }

    public void loadSteps(String testId) {
        this.currentTestId = testId;
        repository.loadSteps(testId, stepsData);
    }

    public void updateStep(String stepId, String valueJson, Boolean applicable) {
        repository.updateStep(stepId, valueJson, applicable, updateStepResult);
    }

    public void refreshSteps() {
        if (currentTestId != null) {
            repository.loadSteps(currentTestId, stepsData);
        }
    }

    /**
     * Guarda una observación o deficiencia para el step indicado.
     *
     * @param stepId        ID del step.
     * @param inspectionId  ID de la inspección (para indexar).
     * @param type          "REMARKS" (Observación) o "DEFICIENCIES" (Deficiencia).
     * @param description   Texto obligatorio.
     * @param photoPath     Ruta local de la foto (null si no aplica).
     */
    public void saveObservation(String stepId, String inspectionId,
                                String type, String description, String photoPath) {
        observationRepository.saveObservation(
                stepId, inspectionId, type, description, photoPath, saveObsResult);
    }

    /**
     * Carga las observaciones de un step desde Room y actualiza el mapa.
     * Usa un MutableLiveData temporal sin observeForever (evita memory leak).
     */
    public void loadObservationsForStep(String stepId) {
        MutableLiveData<Resource<List<ObservationEntity>>> tmp = new MutableLiveData<>();
        // Use a one-shot observer that removes itself after the first non-loading result.
        androidx.lifecycle.Observer<Resource<List<ObservationEntity>>> observer =
                new androidx.lifecycle.Observer<Resource<List<ObservationEntity>>>() {
                    @Override
                    public void onChanged(Resource<List<ObservationEntity>> resource) {
                        if (resource == null || resource.getStatus() == Resource.Status.LOADING) return;
                        tmp.removeObserver(this);
                        if (resource.getStatus() == Resource.Status.SUCCESS
                                && resource.getData() != null) {
                            Map<String, List<ObservationEntity>> current = observationsByStep.getValue();
                            if (current == null) current = new HashMap<>();
                            current.put(stepId, resource.getData());
                            observationsByStep.setValue(new HashMap<>(current));
                        }
                    }
                };
        tmp.observeForever(observer);
        observationRepository.loadObservationsForStep(stepId, tmp);
    }

    /**
     * Agrega una observación ya guardada al mapa local sin recargar desde la API.
     */
    public void appendObservationLocally(ObservationEntity entity) {
        Map<String, List<ObservationEntity>> current = observationsByStep.getValue();
        if (current == null) current = new HashMap<>();
        List<ObservationEntity> list = current.get(entity.testStepId);
        if (list == null) list = new java.util.ArrayList<>();
        list.add(entity);
        current.put(entity.testStepId, list);
        observationsByStep.setValue(new HashMap<>(current));
    }

    public LiveData<Resource<List<StepUiModel>>> getSteps() {
        return stepsData;
    }

    public LiveData<Resource<StepUiModel>> getUpdateStepResult() {
        return updateStepResult;
    }

    public LiveData<Resource<ObservationEntity>> getSaveObsResult() {
        return saveObsResult;
    }

    public LiveData<Map<String, List<ObservationEntity>>> getObservationsByStep() {
        return observationsByStep;
    }

    /**
     * True cuando todos los steps aplicables están completos y válidos.
     */
    public LiveData<Boolean> getCanComplete() {
        return Transformations.map(stepsData, resource -> {
            if (resource == null || resource.getStatus() != Resource.Status.SUCCESS) return false;
            List<StepUiModel> steps = resource.getData();
            return StepValidator.canCompleteTest(steps);
        });
    }

    public void clearUpdateStepResult() {
        updateStepResult.setValue(null);
    }

    public void clearSaveObsResult() {
        saveObsResult.setValue(null);
    }
}
