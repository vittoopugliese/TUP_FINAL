package com.example.tup_final.ui.steps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.repository.StepsRepository;
import com.example.tup_final.util.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StepsViewModel extends ViewModel {

    private final StepsRepository repository;

    private final MutableLiveData<Resource<List<StepUiModel>>> stepsData = new MutableLiveData<>();
    private final MutableLiveData<Resource<StepUiModel>> updateStepResult = new MutableLiveData<>();

    private String currentTestId;

    @Inject
    public StepsViewModel(StepsRepository repository) {
        this.repository = repository;
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

    public LiveData<Resource<List<StepUiModel>>> getSteps() {
        return stepsData;
    }

    public LiveData<Resource<StepUiModel>> getUpdateStepResult() {
        return updateStepResult;
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
}
