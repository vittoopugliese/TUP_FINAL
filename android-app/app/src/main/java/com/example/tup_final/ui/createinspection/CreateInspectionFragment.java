package com.example.tup_final.ui.createinspection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tup_final.R;
import com.example.tup_final.data.remote.dto.BuildingListResponse;
import com.example.tup_final.data.remote.dto.BuildingSummaryResponse;
import com.example.tup_final.data.remote.dto.InspectionTemplateListResponse;
import com.example.tup_final.util.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment para crear una inspección building-wide.
 */
@AndroidEntryPoint
public class CreateInspectionFragment extends Fragment {

    private CreateInspectionViewModel viewModel;
    private TextInputLayout layoutBuilding;
    private TextInputLayout layoutType;
    private TextInputLayout layoutTemplate;
    private AutoCompleteTextView inputBuilding;
    private AutoCompleteTextView inputType;
    private AutoCompleteTextView inputTemplate;
    private MaterialButton btnDate;
    private TextInputEditText inputNotes;
    private TextInputLayout layoutInspector;
    private TextInputEditText inputInspector;
    private TextView textSummary;
    private TextView textInspectorInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_inspection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CreateInspectionViewModel.class);

        layoutBuilding = view.findViewById(R.id.layout_building);
        layoutType = view.findViewById(R.id.layout_type);
        layoutTemplate = view.findViewById(R.id.layout_template);
        inputBuilding = view.findViewById(R.id.input_building);
        inputType = view.findViewById(R.id.input_type);
        inputTemplate = view.findViewById(R.id.input_template);
        btnDate = view.findViewById(R.id.btn_date);
        inputNotes = view.findViewById(R.id.input_notes);
        layoutInspector = view.findViewById(R.id.layout_inspector);
        inputInspector = view.findViewById(R.id.input_inspector);
        textSummary = view.findViewById(R.id.text_summary);
        textInspectorInfo = view.findViewById(R.id.text_inspector_info);

        applyRoleToInspectorSection();
        setupBuildingDropdown();
        setupTypeDropdown();
        setupTemplateDropdown();
        setupDatePicker();
        setupInspectorInput();
        setupInspectorFieldClearListener();
        setupButtons(view);
        observeData();
        observeInspectorPrefill();
    }

    private void applyRoleToInspectorSection() {
        boolean admin = viewModel.isAdminUser();
        layoutInspector.setVisibility(admin ? View.VISIBLE : View.GONE);
        textInspectorInfo.setVisibility(admin ? View.GONE : View.VISIBLE);
    }

    private void clearFormFieldErrors() {
        layoutBuilding.setError(null);
        layoutType.setError(null);
        layoutTemplate.setError(null);
        layoutInspector.setError(null);
    }

    private void setupInspectorFieldClearListener() {
        inputInspector.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutInspector.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void observeInspectorPrefill() {
        if (!viewModel.isAdminUser()) {
            return;
        }
        viewModel.getInspectorEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null && !email.isEmpty() && inputInspector != null
                    && (inputInspector.getText() == null || inputInspector.getText().toString().isEmpty())) {
                inputInspector.setText(email);
            }
        });
    }

    private void setupBuildingDropdown() {
        viewModel.getBuildingsResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                List<BuildingListResponse> buildings = resource.getData();
                List<String> options = new ArrayList<>();
                for (BuildingListResponse b : buildings) {
                    options.add(b.getName() != null ? b.getName() : b.getId());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, options);
                inputBuilding.setAdapter(adapter);
                inputBuilding.setOnItemClickListener((parent, v, position, id) -> {
                    if (position >= 0 && position < buildings.size()) {
                        viewModel.setSelectedBuildingId(buildings.get(position).getId());
                    }
                    layoutBuilding.setError(null);
                });
            }
        });
    }

    private void setupTypeDropdown() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, CreateInspectionViewModel.TYPE_OPTIONS);
        inputType.setAdapter(typeAdapter);
        inputType.setOnItemClickListener((parent, v, position, id) -> {
            if (position >= 0 && position < CreateInspectionViewModel.TYPE_OPTIONS.length) {
                viewModel.setSelectedType(CreateInspectionViewModel.TYPE_OPTIONS[position]);
            }
            layoutType.setError(null);
        });
    }

    private void setupTemplateDropdown() {
        viewModel.getTemplatesResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                List<InspectionTemplateListResponse> templates = resource.getData();
                List<String> names = new ArrayList<>();
                for (InspectionTemplateListResponse t : templates) {
                    names.add(t.getName() != null ? t.getName() : t.getCode());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, names);
                inputTemplate.setAdapter(adapter);
                inputTemplate.setOnItemClickListener((parent, v, position, id) -> {
                    if (position >= 0 && position < templates.size()) {
                        viewModel.setSelectedTemplateId(templates.get(position).getId());
                    }
                    layoutTemplate.setError(null);
                });
            }
        });
    }

    private void setupDatePicker() {
        btnDate.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            MaterialDatePicker<Long> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null) {
                    viewModel.setSelectedDateMillis(selection);
                    SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    format.setTimeZone(TimeZone.getDefault());
                    btnDate.setText(format.format(new Date(selection)));
                }
            });
            picker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupInspectorInput() {
        inputInspector.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && inputInspector.getText() != null) {
                viewModel.setInspectorEmail(inputInspector.getText().toString());
            }
        });
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btn_submit).setOnClickListener(v -> {
            if (inputNotes.getText() != null) {
                viewModel.setNotes(inputNotes.getText().toString());
            }
            if (viewModel.isAdminUser() && inputInspector.getText() != null) {
                viewModel.setInspectorEmail(inputInspector.getText().toString());
            }
            viewModel.createInspection();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void observeData() {
        viewModel.getBuildingSummaryResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                textSummary.setVisibility(View.GONE);
                return;
            }
            if (resource.getStatus() == Resource.Status.LOADING) {
                textSummary.setVisibility(View.VISIBLE);
                textSummary.setText(R.string.create_inspection_summary_loading);
            } else if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                BuildingSummaryResponse summary = resource.getData();
                textSummary.setVisibility(View.VISIBLE);
                textSummary.setText(getString(R.string.create_inspection_summary,
                        summary.getLocationsCount(),
                        summary.getZonesCount(),
                        summary.getDevicesCount(),
                        summary.getTestsCount()));
            } else {
                textSummary.setVisibility(View.GONE);
            }
        });

        viewModel.getCreateResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), R.string.create_inspection_success, Toast.LENGTH_SHORT).show();
                Bundle result = new Bundle();
                if (resource.getData() != null && resource.getData().getId() != null) {
                    result.putString("inspectionId", resource.getData().getId());
                }
                requireActivity().getSupportFragmentManager().setFragmentResult("create_inspection_success", result);
                NavHostFragment.findNavController(this).navigateUp();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                clearFormFieldErrors();
                String msg = resource.getMessage();
                String field = resource.getFormField();
                if (CreateInspectionViewModel.FIELD_BUILDING.equals(field)) {
                    layoutBuilding.setError(msg);
                } else if (CreateInspectionViewModel.FIELD_TYPE.equals(field)) {
                    layoutType.setError(msg);
                } else if (CreateInspectionViewModel.FIELD_TEMPLATE.equals(field)) {
                    layoutTemplate.setError(msg);
                } else if (CreateInspectionViewModel.FIELD_INSPECTOR.equals(field)) {
                    layoutInspector.setError(msg);
                }
                Toast.makeText(requireContext(), msg != null ? msg : getString(R.string.error_unknown),
                        Toast.LENGTH_LONG).show();
            } else if (resource.getStatus() == Resource.Status.LOADING) {
                clearFormFieldErrors();
            }
        });
    }
}
