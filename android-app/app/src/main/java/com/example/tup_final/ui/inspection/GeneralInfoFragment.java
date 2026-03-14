package com.example.tup_final.ui.inspection;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tup_final.R;
import com.example.tup_final.data.entity.InspectionAssignmentEntity;
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.databinding.FragmentGeneralInfoBinding;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GeneralInfoFragment extends Fragment {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private static final String ROLE_INSPECTOR = "INSPECTOR";
    private static final String ROLE_OPERATOR = "OPERATOR";

    private FragmentGeneralInfoBinding binding;
    private InspectionDetailViewModel viewModel;
    private AssignmentAdapter inspectorAdapter;
    private AssignmentAdapter operatorAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGeneralInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireParentFragment()).get(InspectionDetailViewModel.class);

        setupInspectorSection();
        setupOperatorsSection();
        setupTextWatchers();
        observeAssignments();
        observeAddResult();
        observeRemoveResult();

        viewModel.getInspection().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                bindInspection(resource.getData());
            }
        });
    }

    private void setupInspectorSection() {
        binding.recyclerInspector.setLayoutManager(new LinearLayoutManager(requireContext()));
        inspectorAdapter = new AssignmentAdapter(this::onRemoveInspectorClicked);
        binding.recyclerInspector.setAdapter(inspectorAdapter);

        binding.btnAddInspector.setOnClickListener(v -> addInspector());
    }

    private void setupOperatorsSection() {
        binding.recyclerOperators.setLayoutManager(new LinearLayoutManager(requireContext()));
        operatorAdapter = new AssignmentAdapter(this::onRemoveOperatorClicked);
        binding.recyclerOperators.setAdapter(operatorAdapter);

        binding.btnAddOperator.setOnClickListener(v -> addOperator());
    }

    private void setupTextWatchers() {
        TextWatcher inspectorWatcher = createEmailWatcher(() -> updateInspectorAddButton());
        binding.inputInspectorEmail.addTextChangedListener(inspectorWatcher);

        TextWatcher operatorWatcher = createEmailWatcher(() -> updateOperatorAddButton());
        binding.inputOperatorEmail.addTextChangedListener(operatorWatcher);
    }

    private TextWatcher createEmailWatcher(Runnable onChanged) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                onChanged.run();
            }
        };
    }

    private void updateInspectorAddButton() {
        String email = binding.inputInspectorEmail.getText() != null
                ? binding.inputInspectorEmail.getText().toString().trim()
                : "";
        binding.btnAddInspector.setEnabled(isValidEmail(email));
    }

    private void updateOperatorAddButton() {
        String email = binding.inputOperatorEmail.getText() != null
                ? binding.inputOperatorEmail.getText().toString().trim()
                : "";
        binding.btnAddOperator.setEnabled(isValidEmail(email));
    }

    private boolean isValidEmail(String email) {
        return email != null && !email.isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }

    private void addInspector() {
        String inspectionId = viewModel.getCurrentInspectionId();
        if (inspectionId == null) return;

        String email = binding.inputInspectorEmail.getText() != null
                ? binding.inputInspectorEmail.getText().toString().trim()
                : "";
        if (!isValidEmail(email)) {
            Toast.makeText(requireContext(), R.string.assignment_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.addAssignment(inspectionId, email, ROLE_INSPECTOR);
        binding.inputInspectorEmail.setText("");
        updateInspectorAddButton();
    }

    private void addOperator() {
        String inspectionId = viewModel.getCurrentInspectionId();
        if (inspectionId == null) return;

        String email = binding.inputOperatorEmail.getText() != null
                ? binding.inputOperatorEmail.getText().toString().trim()
                : "";
        if (!isValidEmail(email)) {
            Toast.makeText(requireContext(), R.string.assignment_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.addAssignment(inspectionId, email, ROLE_OPERATOR);
        binding.inputOperatorEmail.setText("");
        updateOperatorAddButton();
    }

    private void onRemoveInspectorClicked(InspectionAssignmentEntity assignment) {
        String inspectionId = viewModel.getCurrentInspectionId();
        if (inspectionId == null) return;

        List<InspectionAssignmentEntity> inspectors = viewModel.getInspectorAssignments();
        if (inspectors.size() <= 1) {
            Toast.makeText(requireContext(), R.string.assignment_cannot_remove_inspector, Toast.LENGTH_SHORT).show();
            return;
        }

        showRemoveConfirmation(assignment, inspectionId);
    }

    private void onRemoveOperatorClicked(InspectionAssignmentEntity assignment) {
        String inspectionId = viewModel.getCurrentInspectionId();
        if (inspectionId == null) return;
        showRemoveConfirmation(assignment, inspectionId);
    }

    private void showRemoveConfirmation(InspectionAssignmentEntity assignment, String inspectionId) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.assignment_remove_title)
                .setMessage(getString(R.string.assignment_remove_message, assignment.userEmail))
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        viewModel.removeAssignment(inspectionId, assignment.userEmail))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void observeAssignments() {
        viewModel.getAssignments().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                List<InspectionAssignmentEntity> inspectors = new ArrayList<>();
                List<InspectionAssignmentEntity> operators = new ArrayList<>();
                for (InspectionAssignmentEntity a : resource.getData()) {
                    if (ROLE_INSPECTOR.equals(a.role)) {
                        inspectors.add(a);
                    } else if (ROLE_OPERATOR.equals(a.role)) {
                        operators.add(a);
                    }
                }
                inspectorAdapter.submitList(inspectors);
                operatorAdapter.submitList(operators);

                boolean hasInspector = !inspectors.isEmpty();
                binding.layoutInspectorEmail.setVisibility(hasInspector ? View.GONE : View.VISIBLE);
                binding.btnAddInspector.setVisibility(hasInspector ? View.GONE : View.VISIBLE);
                binding.btnAddInspector.setEnabled(!hasInspector && isValidEmail(
                        binding.inputInspectorEmail.getText() != null
                                ? binding.inputInspectorEmail.getText().toString().trim()
                                : ""));
            }
        });
    }

    private void observeAddResult() {
        viewModel.getAddAssignmentResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.getStatus() == Resource.Status.LOADING) return;
            if (resource.getStatus() == Resource.Status.ERROR && resource.getMessage() != null) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeRemoveResult() {
        viewModel.getRemoveAssignmentResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.getStatus() == Resource.Status.LOADING) return;
            if (resource.getStatus() == Resource.Status.ERROR && resource.getMessage() != null) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindInspection(InspectionEntity inspection) {
        binding.textBuilding.setText(
                inspection.buildingId != null ? inspection.buildingId : "—");

        binding.textType.setText(
                inspection.type != null ? inspection.type : "—");

        binding.textScheduledDate.setText(
                inspection.scheduledDate != null ? formatDate(inspection.scheduledDate) : "—");

        if (inspection.startedAt != null && !inspection.startedAt.isEmpty()) {
            binding.cardStartedAt.setVisibility(View.VISIBLE);
            binding.textStartedAt.setText(formatDate(inspection.startedAt));
        } else {
            binding.cardStartedAt.setVisibility(View.GONE);
        }

        if (inspection.notes != null && !inspection.notes.isEmpty()) {
            binding.cardNotes.setVisibility(View.VISIBLE);
            binding.textNotes.setText(inspection.notes);
        } else {
            binding.cardNotes.setVisibility(View.GONE);
        }

        if (inspection.signer != null && !inspection.signer.isEmpty()) {
            binding.cardSigner.setVisibility(View.VISIBLE);
            binding.textSigner.setText(inspection.signer);
        } else {
            binding.cardSigner.setVisibility(View.GONE);
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null) return "—";
        if (isoDate.contains("T")) {
            return isoDate.replace("T", " ").replaceAll("Z$", "");
        }
        return isoDate;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
