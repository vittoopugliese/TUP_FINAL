package com.example.tup_final.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.repository.AuthRepository;
import com.example.tup_final.sync.SyncScheduler;
import com.example.tup_final.util.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Home fragment con lista de inspecciones y panel de filtros.
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject
    AuthRepository authRepository;

    @Inject
    InspectionDao inspectionDao;

    private HomeViewModel viewModel;
    private InspectionAdapter adapter;
    private ProgressBar progressInspections;
    private TextView textEmptyOrError;
    private RecyclerView recyclerInspections;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        adapter = new InspectionAdapter();

        setupRecyclerView(view);
        setupFilterPanel(view);
        observeData();
        setupLogoutAndProfile(view);
    }

    private void setupRecyclerView(View view) {
        recyclerInspections = view.findViewById(R.id.recycler_inspections);
        progressInspections = view.findViewById(R.id.progress_inspections);
        textEmptyOrError = view.findViewById(R.id.text_empty);
        recyclerInspections.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerInspections.setAdapter(adapter);
    }

    private void setupFilterPanel(View view) {
        AutoCompleteTextView inputBuilding = view.findViewById(R.id.input_filter_building);
        AutoCompleteTextView inputLocation = view.findViewById(R.id.input_filter_location);
        AutoCompleteTextView inputStatus = view.findViewById(R.id.input_filter_status);

        // Status dropdown
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add(getString(R.string.filter_all));
        for (String s : HomeViewModel.STATUS_OPTIONS) {
            statusOptions.add(s);
        }
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, statusOptions);
        inputStatus.setAdapter(statusAdapter);
        inputStatus.setOnItemClickListener((parent, v, position, id) -> {
            String selected = statusOptions.get(position);
            viewModel.setStatusFilter(
                    getString(R.string.filter_all).equals(selected) ? null : selected);
        });

        // Building dropdown - populated when data loads
        viewModel.getBuildingIdsResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                List<String> buildingIds = new ArrayList<>(resource.getData());
                buildingIds.add(0, getString(R.string.filter_all));
                ArrayAdapter<String> buildingAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, buildingIds);
                inputBuilding.setAdapter(buildingAdapter);
                inputBuilding.setOnItemClickListener((parent, v, position, id) -> {
                    String selected = buildingIds.get(position);
                    viewModel.setBuildingFilter(
                            getString(R.string.filter_all).equals(selected) ? null : selected);
                });
            }
        });

        // Location dropdown - populated when data loads
        viewModel.getLocationIdsResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                List<String> locationIds = new ArrayList<>(resource.getData());
                locationIds.add(0, getString(R.string.filter_all));
                ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, locationIds);
                inputLocation.setAdapter(locationAdapter);
                inputLocation.setOnItemClickListener((parent, v, position, id) -> {
                    String selected = locationIds.get(position);
                    viewModel.setLocationFilter(
                            getString(R.string.filter_all).equals(selected) ? null : selected);
                });
            }
        });

        // Date filter button
        view.findViewById(R.id.btn_filter_date).setOnClickListener(v -> showDateRangePicker());

        // Apply and Clear buttons
        view.findViewById(R.id.btn_filter_apply).setOnClickListener(v -> viewModel.applyFilters());
        view.findViewById(R.id.btn_filter_clear).setOnClickListener(v -> {
            viewModel.clearFilters();
            inputBuilding.setText("", false);
            inputLocation.setText("", false);
            inputStatus.setText("", false);
            ((MaterialButton) view.findViewById(R.id.btn_filter_date)).setText(R.string.filter_date_hint);
        });
    }

    private void showDateRangePicker() {
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                MaterialDatePicker.Builder.dateRangePicker();
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(
                (MaterialPickerOnPositiveButtonClickListener<androidx.core.util.Pair<Long, Long>>) selection -> {
                    if (selection != null) {
                        Long fromMillis = selection.first;
                        Long toMillis = selection.second;
                        viewModel.setDateFilter(fromMillis, toMillis);
                    }
                });

        picker.show(getParentFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void observeData() {
        viewModel.getFilteredInspections().observe(getViewLifecycleOwner(), inspections -> {
            if (inspections != null) {
                adapter.submitList(new ArrayList<>(inspections));
            }
        });

        viewModel.getInspectionsResult().observe(getViewLifecycleOwner(), resource -> {
            onInspectionsChanged(resource);
            if (resource != null && resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(),
                        resource.getMessage() != null ? resource.getMessage() : getString(R.string.error_unknown),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLogoutAndProfile(View view) {
        getParentFragmentManager().setFragmentResultListener(
                LogoutDialogFragment.REQUEST_KEY, this, (key, result) -> {
                    boolean syncFirst = result.getBoolean(LogoutDialogFragment.RESULT_SYNC_FIRST, false);
                    if (syncFirst) {
                        SyncScheduler.enqueueOneTime(requireContext());
                    }
                    Executors.newSingleThreadExecutor().execute(() -> {
                        authRepository.logout();
                        requireActivity().runOnUiThread(() ->
                                NavHostFragment.findNavController(HomeFragment.this)
                                        .navigate(R.id.action_home_to_login)
                        );
                    });
                });

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            boolean hasPendingData = false;
            LogoutDialogFragment.newInstance(hasPendingData)
                    .show(getParentFragmentManager(), "LogoutDialog");
        });

        view.findViewById(R.id.btn_profile).setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_home_to_profile));

        view.findViewById(R.id.btn_inspections).setOnClickListener(v ->
                Executors.newSingleThreadExecutor().execute(() -> {
                    List<InspectionEntity> inspections = inspectionDao.getAll();
                    requireActivity().runOnUiThread(() -> {
                        if (inspections != null && !inspections.isEmpty()) {
                            Bundle args = new Bundle();
                            args.putString("inspectionId", inspections.get(0).id);
                            NavHostFragment.findNavController(HomeFragment.this)
                                    .navigate(R.id.action_home_to_inspectionDetail, args);
                        } else {
                            Toast.makeText(requireContext(),
                                    R.string.inspection_no_inspections,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }));
    }

    private void onInspectionsChanged(Resource<List<InspectionEntity>> resource) {
        if (resource == null) return;

        switch (resource.getStatus()) {
            case LOADING:
                progressInspections.setVisibility(View.VISIBLE);
                textEmptyOrError.setVisibility(View.GONE);
                recyclerInspections.setVisibility(View.GONE);
                break;
            case SUCCESS:
                progressInspections.setVisibility(View.GONE);
                List<InspectionEntity> list = resource.getData();
                if (list == null || list.isEmpty()) {
                    textEmptyOrError.setVisibility(View.VISIBLE);
                    textEmptyOrError.setText(R.string.inspections_empty);
                    recyclerInspections.setVisibility(View.GONE);
                } else {
                    textEmptyOrError.setVisibility(View.GONE);
                    recyclerInspections.setVisibility(View.VISIBLE);
                }
                break;
            case ERROR:
                progressInspections.setVisibility(View.GONE);
                textEmptyOrError.setVisibility(View.VISIBLE);
                textEmptyOrError.setText(resource.getMessage() != null
                        ? resource.getMessage() : getString(R.string.inspections_error));
                recyclerInspections.setVisibility(View.GONE);
                break;
        }
    }
}
