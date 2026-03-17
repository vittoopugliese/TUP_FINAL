package com.example.tup_final.ui.steps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.example.tup_final.ui.inspectiontests.InspectionTestsFragment;
import com.example.tup_final.util.Resource;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

@AndroidEntryPoint
public class StepsFragment extends Fragment {

    private static final String ARG_INSPECTION_ID = "inspectionId";
    private static final String ARG_TEST_ID = "testId";
    private static final String ARG_DEVICE_ID = "deviceId";

    private StepsViewModel viewModel;
    private StepsAdapter adapter;
    private ProgressBar progress;
    private TextView textEmpty;
    private TextView textError;
    private RecyclerView recyclerSteps;
    private MaterialButton btnComplete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_steps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String testId = getArguments() != null ? getArguments().getString(ARG_TEST_ID, "") : "";
        String inspectionId = getArguments() != null ? getArguments().getString(ARG_INSPECTION_ID, "") : "";

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        progress = view.findViewById(R.id.progress_steps);
        textEmpty = view.findViewById(R.id.text_steps_empty);
        textError = view.findViewById(R.id.text_steps_error);
        recyclerSteps = view.findViewById(R.id.recycler_steps);
        btnComplete = view.findViewById(R.id.btn_complete);

        if (testId == null || testId.trim().isEmpty()) {
            showError(getString(R.string.steps_invalid_test));
            return;
        }

        viewModel = new ViewModelProvider(this).get(StepsViewModel.class);
        adapter = new StepsAdapter();
        adapter.setOnStepValueChangeListener((step, valueJson, applicable) ->
                viewModel.updateStep(step.id, valueJson, applicable));

        recyclerSteps.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerSteps.setAdapter(adapter);

        btnComplete.setOnClickListener(v -> {
            if (viewModel.getCanComplete().getValue() != Boolean.TRUE) return;
            requireActivity().getSupportFragmentManager()
                    .setFragmentResult(InspectionTestsFragment.RESULT_KEY_STEPS_COMPLETED, new Bundle());
            NavHostFragment.findNavController(this).popBackStack();
        });

        viewModel.loadSteps(testId);
        observeSteps();
        observeUpdateStep();
        observeCanComplete();
    }

    private void observeSteps() {
        viewModel.getSteps().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    progress.setVisibility(View.VISIBLE);
                    textEmpty.setVisibility(View.GONE);
                    textError.setVisibility(View.GONE);
                    recyclerSteps.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    progress.setVisibility(View.GONE);
                    textError.setVisibility(View.GONE);
                    List<StepUiModel> steps = resource.getData();
                    if (steps == null || steps.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                        recyclerSteps.setVisibility(View.GONE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                        recyclerSteps.setVisibility(View.VISIBLE);
                        adapter.submitList(new ArrayList<>(steps));
                    }
                    break;
                case ERROR:
                    progress.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.GONE);
                    recyclerSteps.setVisibility(View.GONE);
                    showError(resource.getMessage() != null ? resource.getMessage() : getString(R.string.steps_not_available));
                    break;
            }
        });
    }

    private void observeCanComplete() {
        viewModel.getCanComplete().observe(getViewLifecycleOwner(), canComplete -> {
            btnComplete.setEnabled(Boolean.TRUE.equals(canComplete));
        });
    }

    private void observeUpdateStep() {
        viewModel.getUpdateStepResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.getStatus() == SUCCESS) {
                viewModel.clearUpdateStepResult();
                viewModel.refreshSteps();
            } else if (resource.getStatus() == ERROR) {
                Toast.makeText(requireContext(),
                        resource.getMessage() != null ? resource.getMessage() : getString(R.string.error_unknown),
                        Toast.LENGTH_SHORT).show();
                viewModel.clearUpdateStepResult();
            }
        });
    }

    private void showError(String message) {
        progress.setVisibility(View.GONE);
        textEmpty.setVisibility(View.GONE);
        recyclerSteps.setVisibility(View.GONE);
        textError.setVisibility(View.VISIBLE);
        textError.setText(message);
    }
}
