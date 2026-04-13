package com.example.tup_final.ui.auditlog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tup_final.R;
import com.example.tup_final.data.remote.dto.AuditLogResponse;
import com.example.tup_final.databinding.FragmentAuditLogBinding;
import com.example.tup_final.util.Resource;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import dagger.hilt.android.AndroidEntryPoint;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

@AndroidEntryPoint
public class AuditLogFragment extends Fragment {

    private FragmentAuditLogBinding binding;
    private AuditLogViewModel viewModel;
    private AuditLogAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAuditLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AuditLogViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        adapter = new AuditLogAdapter();
        binding.recyclerAuditLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAuditLogs.setAdapter(adapter);

        setupActionDropdown();
        setupDateButton();
        binding.btnApplyFilters.setOnClickListener(v -> viewModel.loadLogs());
        binding.btnClearFilters.setOnClickListener(v -> {
            viewModel.clearDateRange();
            binding.btnFilterDate.setText(R.string.audit_filter_date_hint);
            viewModel.setActionFilterKey(AuditLogViewModel.FILTER_ACTION_ALL);
            binding.inputFilterAction.setText(getString(R.string.audit_action_all), false);
            viewModel.loadLogs();
        });

        observeAdminGuard();
        observeLogs();
    }

    private void setupActionDropdown() {
        String[] labels = new String[]{
                getString(R.string.audit_action_all),
                AuditLogViewModel.ACTION_ROLE_CHANGE,
                AuditLogViewModel.ACTION_ASSIGNMENT_ADD,
                AuditLogViewModel.ACTION_ASSIGNMENT_REMOVE,
                AuditLogViewModel.ACTION_CREATE,
                AuditLogViewModel.ACTION_SIGN
        };
        ArrayAdapter<String> actionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                labels);
        binding.inputFilterAction.setAdapter(actionAdapter);
        binding.inputFilterAction.setText(labels[0], false);
        viewModel.setActionFilterKey(AuditLogViewModel.FILTER_ACTION_ALL);
        binding.inputFilterAction.setOnItemClickListener((parent, v, position, id) -> {
            String key = AuditLogViewModel.FILTER_ACTION_ALL;
            if (position == 1) {
                key = AuditLogViewModel.ACTION_ROLE_CHANGE;
            } else if (position == 2) {
                key = AuditLogViewModel.ACTION_ASSIGNMENT_ADD;
            } else if (position == 3) {
                key = AuditLogViewModel.ACTION_ASSIGNMENT_REMOVE;
            } else if (position == 4) {
                key = AuditLogViewModel.ACTION_CREATE;
            } else if (position == 5) {
                key = AuditLogViewModel.ACTION_SIGN;
            }
            viewModel.setActionFilterKey(key);
        });
    }

    private void setupDateButton() {
        binding.btnFilterDate.setOnClickListener(v -> {
            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                    MaterialDatePicker.Builder.dateRangePicker();
            MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
            picker.addOnPositiveButtonClickListener(
                    (MaterialPickerOnPositiveButtonClickListener<androidx.core.util.Pair<Long, Long>>) selection -> {
                        if (selection != null) {
                            Long fromMillis = selection.first;
                            Long toMillis = selection.second;
                            viewModel.setDateRangeMillis(fromMillis, toMillis);
                            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String fromFormatted = format.format(new Date(fromMillis));
                            String toFormatted = format.format(new Date(toMillis));
                            binding.btnFilterDate.setText(fromFormatted + " - " + toFormatted);
                        }
                    });
            picker.show(getParentFragmentManager(), "AUDIT_DATE_RANGE");
        });
    }

    private void observeAdminGuard() {
        viewModel.getIsAdminAllowed().observe(getViewLifecycleOwner(), allowed -> {
            if (allowed != null && !allowed) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void observeLogs() {
        viewModel.getLogsResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || binding == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressAudit.setVisibility(View.VISIBLE);
                    binding.textEmpty.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressAudit.setVisibility(View.GONE);
                    List<AuditLogResponse> list = resource.getData();
                    if (list == null || list.isEmpty()) {
                        binding.textEmpty.setVisibility(View.VISIBLE);
                        binding.recyclerAuditLogs.setVisibility(View.GONE);
                        adapter.submitList(new ArrayList<>());
                    } else {
                        binding.textEmpty.setVisibility(View.GONE);
                        binding.recyclerAuditLogs.setVisibility(View.VISIBLE);
                        adapter.submitList(new ArrayList<>(list));
                    }
                    break;
                case ERROR:
                    binding.progressAudit.setVisibility(View.GONE);
                    binding.textEmpty.setVisibility(View.VISIBLE);
                    binding.recyclerAuditLogs.setVisibility(View.GONE);
                    binding.textEmpty.setText(resource.getMessage() != null
                            ? resource.getMessage() : getString(R.string.error_unknown));
                    adapter.submitList(new ArrayList<>());
                    Toast.makeText(requireContext(),
                            resource.getMessage() != null ? resource.getMessage() : getString(R.string.error_unknown),
                            Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
