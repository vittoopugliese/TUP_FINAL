package com.example.tup_final.ui.auditlog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tup_final.R;
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.remote.dto.AuditLogResponse;
import com.example.tup_final.data.remote.dto.UserProfileResponse;
import com.example.tup_final.databinding.FragmentAuditLogBinding;
import com.example.tup_final.util.Resource;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuditLogFragment extends Fragment {

    private FragmentAuditLogBinding binding;
    private AuditLogViewModel viewModel;
    private AuditLogAdapter adapter;

    private List<String> inspectionIds = new ArrayList<>();
    private List<String> userEmails = new ArrayList<>();

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
        setupExportButton();

        binding.btnApplyFilters.setOnClickListener(v -> viewModel.loadLogs());
        binding.btnClearFilters.setOnClickListener(v -> {
            viewModel.clearAllFilters();
            binding.btnFilterDate.setText(R.string.audit_filter_date_hint);
            binding.inputFilterAction.setText(getString(R.string.audit_action_all), false);
            binding.inputFilterUser.setText(getString(R.string.audit_filter_user_all), false);
            binding.inputFilterInspection.setText(getString(R.string.audit_filter_inspection_all), false);
            viewModel.loadLogs();
        });

        observeAdminGuard();
        observeDropdownData();
        observeLogs();
        observePdfResult();
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
        binding.inputFilterAction.setOnItemClickListener((parent, v, position, id) -> {
            String key = AuditLogViewModel.FILTER_ACTION_ALL;
            if (position == 1) key = AuditLogViewModel.ACTION_ROLE_CHANGE;
            else if (position == 2) key = AuditLogViewModel.ACTION_ASSIGNMENT_ADD;
            else if (position == 3) key = AuditLogViewModel.ACTION_ASSIGNMENT_REMOVE;
            else if (position == 4) key = AuditLogViewModel.ACTION_CREATE;
            else if (position == 5) key = AuditLogViewModel.ACTION_SIGN;
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

    private void setupExportButton() {
        binding.btnExportPdf.setOnClickListener(v -> viewModel.exportPdf());
    }

    private void observeDropdownData() {
        viewModel.getUsersForDropdown().observe(getViewLifecycleOwner(), users -> {
            if (binding == null || users == null) return;
            userEmails.clear();
            List<String> labels = new ArrayList<>();
            labels.add(getString(R.string.audit_filter_user_all));
            for (UserProfileResponse u : users) {
                labels.add(u.getEmail());
                userEmails.add(u.getEmail());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, labels);
            binding.inputFilterUser.setAdapter(adapter);
            binding.inputFilterUser.setText(labels.get(0), false);
            binding.inputFilterUser.setOnItemClickListener((parent, v, pos, id) -> {
                if (pos == 0) {
                    viewModel.setUserFilter(null);
                } else {
                    viewModel.setUserFilter(userEmails.get(pos - 1));
                }
            });
        });

        viewModel.getInspectionsForDropdown().observe(getViewLifecycleOwner(), inspections -> {
            if (binding == null || inspections == null) return;
            inspectionIds.clear();
            List<String> labels = new ArrayList<>();
            labels.add(getString(R.string.audit_filter_inspection_all));
            for (InspectionEntity insp : inspections) {
                String shortId = insp.id.length() > 8 ? insp.id.substring(0, 8) : insp.id;
                String bName = insp.buildingName != null ? insp.buildingName : "?";
                labels.add(bName + " (" + shortId + ")");
                inspectionIds.add(insp.id);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, labels);
            binding.inputFilterInspection.setAdapter(adapter);
            binding.inputFilterInspection.setText(labels.get(0), false);
            binding.inputFilterInspection.setOnItemClickListener((parent, v, pos, id) -> {
                if (pos == 0) {
                    viewModel.setEntityIdFilter(null);
                } else {
                    viewModel.setEntityIdFilter(inspectionIds.get(pos - 1));
                }
            });
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

    private void observePdfResult() {
        viewModel.getPdfResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || binding == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.btnExportPdf.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.btnExportPdf.setEnabled(true);
                    byte[] data = resource.getData();
                    if (data != null && data.length > 0) {
                        openPdf(data);
                    }
                    break;
                case ERROR:
                    binding.btnExportPdf.setEnabled(true);
                    Toast.makeText(requireContext(),
                            resource.getMessage() != null
                                    ? resource.getMessage()
                                    : getString(R.string.audit_export_error),
                            Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void openPdf(byte[] pdfBytes) {
        try {
            File cacheDir = requireContext().getCacheDir();
            File pdfFile = new File(cacheDir, "audit-report.pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                fos.write(pdfBytes);
            }
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (Exception e) {
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setType("application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, getString(R.string.audit_export_pdf)));
            }
            Toast.makeText(requireContext(), getString(R.string.audit_export_success), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), getString(R.string.audit_export_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
