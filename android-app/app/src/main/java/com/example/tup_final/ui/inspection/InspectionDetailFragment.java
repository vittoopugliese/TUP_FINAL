package com.example.tup_final.ui.inspection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.tup_final.R;
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.databinding.FragmentInspectionDetailBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InspectionDetailFragment extends Fragment {

    private static final String ARG_INSPECTION_ID = "inspectionId";

    private FragmentInspectionDetailBinding binding;
    private InspectionDetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInspectionDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InspectionDetailViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        setupTabs();

        String inspectionId = getArguments() != null
                ? getArguments().getString(ARG_INSPECTION_ID) : null;

        if (inspectionId == null || inspectionId.isEmpty()) {
            showError(getString(R.string.inspection_error_not_found));
            return;
        }

        viewModel.loadInspection(inspectionId);
        observeInspection();
        observeStartResult();

        binding.btnStartInspection.setOnClickListener(v ->
                viewModel.startOrContinueInspection());
    }

    private void setupTabs() {
        InspectionDetailPagerAdapter pagerAdapter =
                new InspectionDetailPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        String[] tabTitles = {
                getString(R.string.tab_general_info),
                getString(R.string.tab_devices)
        };

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    private void observeInspection() {
        viewModel.getInspection().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressDetail.setVisibility(View.VISIBLE);
                    setContentVisibility(View.GONE);
                    binding.textError.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressDetail.setVisibility(View.GONE);
                    binding.textError.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        setContentVisibility(View.VISIBLE);
                        bindHeader(resource.getData());
                    }
                    break;

                case ERROR:
                    binding.progressDetail.setVisibility(View.GONE);
                    setContentVisibility(View.GONE);
                    showError(resource.getMessage());
                    break;
            }
        });
    }

    private void observeStartResult() {
        viewModel.getStartResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.btnStartInspection.setEnabled(false);
                    break;

                case SUCCESS:
                    binding.btnStartInspection.setEnabled(true);
                    if (resource.getData() != null) {
                        bindHeader(resource.getData());
                        navigateToLocations(resource.getData());
                    }
                    break;

                case ERROR:
                    binding.btnStartInspection.setEnabled(true);
                    showValidationError(resource.getMessage());
                    break;
            }
        });
    }

    private void bindHeader(InspectionEntity inspection) {
        binding.chipStatus.setText(inspection.status != null ? inspection.status : "—");
        setStatusChipColor(inspection.status);

        String typeLabel = inspection.type != null ? inspection.type : "—";
        binding.textInspectionType.setText(typeLabel);

        boolean isInProgress = "IN_PROGRESS".equals(inspection.status);
        boolean isDone = inspection.status != null && inspection.status.startsWith("DONE");

        if (isDone) {
            binding.btnStartInspection.setVisibility(View.GONE);
        } else if (isInProgress) {
            binding.btnStartInspection.setText(R.string.btn_continue_inspection);
            binding.btnStartInspection.setVisibility(View.VISIBLE);
        } else {
            binding.btnStartInspection.setText(R.string.btn_start_inspection);
            binding.btnStartInspection.setVisibility(View.VISIBLE);
        }
    }

    private void setStatusChipColor(String status) {
        int colorRes;
        if (status == null) {
            colorRes = android.R.color.darker_gray;
        } else {
            switch (status) {
                case "IN_PROGRESS":
                    colorRes = android.R.color.holo_blue_light;
                    break;
                case "DONE_COMPLETED":
                    colorRes = android.R.color.holo_green_light;
                    break;
                case "DONE_FAILED":
                    colorRes = android.R.color.holo_red_light;
                    break;
                default:
                    colorRes = android.R.color.holo_orange_light;
                    break;
            }
        }
        binding.chipStatus.setChipBackgroundColorResource(colorRes);
    }

    private void navigateToLocations(InspectionEntity inspection) {
        Bundle args = new Bundle();
        args.putString("inspectionId", inspection.id);
        args.putString("buildingId", inspection.buildingId != null ? inspection.buildingId : "");
        Navigation.findNavController(requireView())
                .navigate(R.id.action_inspectionDetail_to_locations, args);
    }

    private void showValidationError(String message) {
        if (message == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.inspection_validation_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showError(String message) {
        binding.textError.setVisibility(View.VISIBLE);
        binding.textError.setText(message != null ? message : getString(R.string.error_unknown));
    }

    private void setContentVisibility(int visibility) {
        binding.chipStatus.setVisibility(visibility);
        binding.textInspectionType.setVisibility(visibility);
        binding.tabLayout.setVisibility(visibility);
        binding.viewPager.setVisibility(visibility);
        binding.btnStartInspection.setVisibility(visibility);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
