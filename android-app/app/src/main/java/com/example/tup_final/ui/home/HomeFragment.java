package com.example.tup_final.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.tup_final.util.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Home fragment con lista de inspecciones y panel de filtros.
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private InspectionAdapter adapter;
    private MaterialButton btnFilterDate;
    private ProgressBar progressInspections;
    private TextView textEmptyOrError;
    private TextView textInspectionsCount;
    private RecyclerView recyclerInspections;
    private View filterContent;
    private ImageView iconFilterChevron;
    private boolean filtersExpanded = false;
    private AutoCompleteTextView inputBuilding;
    private AutoCompleteTextView inputStatus;
    private TextView textActiveFilters;
    private String pendingScrollToInspectionId = null;

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
        adapter.setOnInspectionClickListener(inspection -> {
            Bundle args = new Bundle();
            args.putString("inspectionId", inspection.id);
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_home_to_inspectionDetail, args);
        });

        setupRecyclerView(view);
        setupFilterPanel(view);
        setupFilterCollapse(view);
        setupFab(view);
        setupManageUsersButton(view);
        setupAuditLogButton(view);
        observeData();
        observeActiveFilters();
        observeCurrentUserRole();
        setupFragmentResultListener();
    }

    private void setupFab(View view) {
        view.findViewById(R.id.fab_create_inspection).setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_home_to_createInspection));
    }

    private void setupManageUsersButton(View view) {
        View btnManageUsers = view.findViewById(R.id.btn_manage_users);
        btnManageUsers.setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_home_to_userManagement));
    }

    private void setupAuditLogButton(View view) {
        view.findViewById(R.id.btn_audit_log).setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_home_to_auditLog));
    }

    private void observeCurrentUserRole() {
        viewModel.getProfileForRole().observe(getViewLifecycleOwner(), r -> {});
        viewModel.getCurrentUserRole().observe(getViewLifecycleOwner(), role -> {
            View root = getView();
            if (root == null) return;
            View btnManage = root.findViewById(R.id.btn_manage_users);
            View btnAudit = root.findViewById(R.id.btn_audit_log);
            View fabCreate = root.findViewById(R.id.fab_create_inspection);
            if (btnAudit != null) {
                btnAudit.setVisibility("ADMIN".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);
            }
            if (btnManage != null) {
                btnManage.setVisibility("ADMIN".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);
            }
            if (fabCreate != null) {
                boolean canCreate = "ADMIN".equalsIgnoreCase(role) || "INSPECTOR".equalsIgnoreCase(role);
                fabCreate.setVisibility(canCreate ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupFragmentResultListener() {
        requireActivity().getSupportFragmentManager().setFragmentResultListener("create_inspection_success",
                getViewLifecycleOwner(), (requestKey, result) -> {
                    pendingScrollToInspectionId = result != null ? result.getString("inspectionId") : null;
                    viewModel.clearFilters();
                    resetFilterInputs();
                    viewModel.loadInspections();
                });
    }

    private void resetFilterInputs() {
        if (inputBuilding != null) inputBuilding.setText("", false);
        if (inputStatus != null) inputStatus.setText("", false);
        if (btnFilterDate != null) btnFilterDate.setText(R.string.filter_date_hint);
    }

    private void scrollToInspection(String inspectionId, List<InspectionEntity> inspections) {
        if (inspectionId == null || inspections == null || recyclerInspections == null) return;
        for (int i = 0; i < inspections.size(); i++) {
            if (inspectionId.equals(inspections.get(i).id)) {
                final int position = i;
                recyclerInspections.post(() -> recyclerInspections.smoothScrollToPosition(position));
                return;
            }
        }
    }

    private void setupFilterCollapse(View view) {
        filterContent = view.findViewById(R.id.filter_content);
        iconFilterChevron = view.findViewById(R.id.icon_filter_chevron);
        View filterHeader = view.findViewById(R.id.filter_header);

        collapseFilters();

        filterHeader.setOnClickListener(v -> {
            if (filtersExpanded) {
                collapseFilters();
            } else {
                filtersExpanded = true;
                filterContent.setVisibility(View.VISIBLE);
                iconFilterChevron.setImageResource(R.drawable.ic_chevron_up);
                iconFilterChevron.setContentDescription(getString(R.string.filter_collapse_hint));
            }
        });
    }

    private void collapseFilters() {
        filtersExpanded = false;
        filterContent.setVisibility(View.GONE);
        iconFilterChevron.setImageResource(R.drawable.ic_chevron_down);
        iconFilterChevron.setContentDescription(getString(R.string.filter_expand_hint));
    }

    private void setupRecyclerView(View view) {
        recyclerInspections = view.findViewById(R.id.recycler_inspections);
        progressInspections = view.findViewById(R.id.progress_inspections);
        textEmptyOrError = view.findViewById(R.id.text_empty);
        textInspectionsCount = view.findViewById(R.id.text_inspections_count);
        textActiveFilters = view.findViewById(R.id.text_active_filters);
        recyclerInspections.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerInspections.setAdapter(adapter);
    }

    private void setupFilterPanel(View view) {
        inputBuilding = view.findViewById(R.id.input_filter_building);
        inputStatus = view.findViewById(R.id.input_filter_status);

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
            collapseFilters();
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
                    collapseFilters();
                });
            }
        });

        // Date filter button
        btnFilterDate = view.findViewById(R.id.btn_filter_date);
        btnFilterDate.setOnClickListener(v -> showDateRangePicker());

        // Apply and Clear buttons
        view.findViewById(R.id.btn_filter_apply).setOnClickListener(v -> viewModel.applyFilters());
        view.findViewById(R.id.btn_filter_clear).setOnClickListener(v -> {
            viewModel.clearFilters();
            resetFilterInputs();
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
                        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String fromFormatted = format.format(new Date(fromMillis));
                        String toFormatted = format.format(new Date(toMillis));
                        btnFilterDate.setText(fromFormatted + " - " + toFormatted);
                    }
                });

        picker.show(getParentFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void observeData() {
        viewModel.getFilteredInspections().observe(getViewLifecycleOwner(), inspections -> {
            if (inspections != null) {
                adapter.submitList(new ArrayList<>(inspections), () -> {
                    if (pendingScrollToInspectionId != null) {
                        scrollToInspection(pendingScrollToInspectionId, inspections);
                        pendingScrollToInspectionId = null;
                    }
                });
                textInspectionsCount.setText(getString(R.string.inspections_count, inspections.size()));
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

    private void observeActiveFilters() {
        androidx.lifecycle.Observer<Object> updateSummary = o -> updateActiveFiltersSummary();
        viewModel.getStatusFilter().observe(getViewLifecycleOwner(), updateSummary);
        viewModel.getBuildingFilter().observe(getViewLifecycleOwner(), updateSummary);
        viewModel.getDateFromFilter().observe(getViewLifecycleOwner(), updateSummary);
        viewModel.getDateToFilter().observe(getViewLifecycleOwner(), updateSummary);
    }

    private void updateActiveFiltersSummary() {
        String status = viewModel.getStatusFilter().getValue();
        String building = viewModel.getBuildingFilter().getValue();
        Long dateFrom = viewModel.getDateFromFilter().getValue();
        Long dateTo = viewModel.getDateToFilter().getValue();

        List<String> parts = new ArrayList<>();
        if (status != null) parts.add(getString(R.string.filter_active_status, status));
        if (building != null) parts.add(getString(R.string.filter_active_building, building));
        if (dateFrom != null && dateTo != null) {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String range = format.format(new Date(dateFrom)) + " - " + format.format(new Date(dateTo));
            parts.add(getString(R.string.filter_active_date, range));
        }

        if (parts.isEmpty()) {
            textActiveFilters.setVisibility(View.GONE);
        } else {
            textActiveFilters.setText(android.text.TextUtils.join(" | ", parts));
            textActiveFilters.setVisibility(View.VISIBLE);
        }
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
