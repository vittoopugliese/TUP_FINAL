package com.example.tup_final.ui.inspection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.tup_final.R;
import com.example.tup_final.data.entity.InspectionAssignmentEntity;
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.databinding.FragmentInspectionDetailBinding;
import com.example.tup_final.util.Resource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import static com.example.tup_final.util.Resource.Status.SUCCESS;

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
        observeAssignments();
        observeStartResult();
        observeSignResult();

        binding.btnStartInspection.setOnClickListener(v ->
                viewModel.startOrContinueInspection());

        binding.btnSignInspection.setOnClickListener(v -> onSignClicked());
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

    // ── Observers ─────────────────────────────────────────────────────────────

    private void observeInspection() {
        viewModel.getInspection().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || binding == null) return;
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

    private void observeAssignments() {
        viewModel.getAssignments().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.getStatus() == SUCCESS && resource.getData() != null) {
                Resource<InspectionEntity> inspRes = viewModel.getInspection().getValue();
                if (inspRes != null && inspRes.getData() != null) {
                    List<InspectionAssignmentEntity> inspectors = filterInspectors(resource.getData());
                    updateStartButton(inspRes.getData(), inspectors);
                    updateSignButton(inspRes.getData());
                }
            }
        });
    }

    private void observeStartResult() {
        viewModel.getStartResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.btnStartInspection.setEnabled(false);
                    break;

                case SUCCESS:
                    binding.btnStartInspection.setEnabled(true);
                    if (resource.getData() != null) {
                        bindHeader(resource.getData());
                        if (viewModel.consumeStartResultForNavigation(resource)) {
                            navigateToLocations(resource.getData());
                        }
                    }
                    break;

                case ERROR:
                    binding.btnStartInspection.setEnabled(true);
                    showValidationError(resource.getMessage());
                    break;
            }
        });
    }

    private void observeSignResult() {
        viewModel.getSignResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || binding == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.btnSignInspection.setEnabled(false);
                    binding.btnStartInspection.setEnabled(false);
                    break;

                case SUCCESS:
                    binding.btnSignInspection.setEnabled(true);
                    binding.btnStartInspection.setEnabled(true);
                    if (resource.getData() != null) {
                        bindHeader(resource.getData());
                        Toast.makeText(requireContext(),
                                getString(R.string.sign_success), Toast.LENGTH_SHORT).show();
                    }
                    break;

                case ERROR:
                    binding.btnSignInspection.setEnabled(true);
                    binding.btnStartInspection.setEnabled(true);
                    showValidationError(resource.getMessage() != null
                            ? resource.getMessage()
                            : getString(R.string.sign_error_generic));
                    break;
            }
        });
    }

    // ── Signing ──────────────────────────────────────────────────────────────

    private void onSignClicked() {
        String inspectionId = viewModel.getCurrentInspectionId();
        if (inspectionId == null) return;

        viewModel.validateAllTestsDone(inspectionId, allDone -> {
            if (!isAdded() || binding == null) return;
            if (!allDone) {
                showValidationError(getString(R.string.sign_error_tests_pending));
                return;
            }
            showSignConfirmationDialog();
        });
    }

    private void showSignConfirmationDialog() {
        if (!isAdded()) return;

        viewModel.resolveSignerName(signerName -> {
            if (!isAdded()) return;
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.sign_dialog_title)
                    .setMessage(getString(R.string.sign_dialog_message))
                    .setNegativeButton(R.string.btn_cancel, null)
                    .setPositiveButton(R.string.sign_dialog_confirm, (dialog, which) ->
                            viewModel.signInspection(signerName))
                    .show();
        });
    }

    // ── Bind UI ──────────────────────────────────────────────────────────────

    private void bindHeader(InspectionEntity inspection) {
        binding.chipStatus.setText(inspection.status != null ? inspection.status : "—");
        setStatusChipColor(inspection.status);

        String typeLabel = inspection.type != null ? inspection.type : "—";
        binding.textInspectionType.setText(typeLabel);

        List<InspectionAssignmentEntity> inspectors = viewModel.getInspectorAssignments();
        updateStartButton(inspection, inspectors);
        updateSignButton(inspection);
        updateSignedInfo(inspection);
    }

    private List<InspectionAssignmentEntity> filterInspectors(List<InspectionAssignmentEntity> assignments) {
        List<InspectionAssignmentEntity> result = new ArrayList<>();
        if (assignments == null) return result;
        for (InspectionAssignmentEntity a : assignments) {
            if ("INSPECTOR".equals(a.role)) result.add(a);
        }
        return result;
    }

    private void updateStartButton(InspectionEntity inspection, List<InspectionAssignmentEntity> inspectors) {
        if (inspection == null) return;

        boolean isSigned = inspection.signed;
        if (isSigned) {
            binding.btnStartInspection.setText(R.string.btn_continue_inspection);
            binding.btnStartInspection.setEnabled(true);
            binding.btnStartInspection.setVisibility(View.VISIBLE);
            return;
        }

        binding.btnStartInspection.setVisibility(View.VISIBLE);
        boolean showStart = viewModel.shouldShowStartLabel(inspection, inspectors);
        binding.btnStartInspection.setText(showStart
                ? R.string.btn_start_inspection : R.string.btn_continue_inspection);
        binding.btnStartInspection.setEnabled(viewModel.isStartButtonEnabled(inspection, inspectors));
    }

    private void updateSignButton(InspectionEntity inspection) {
        if (inspection == null) {
            binding.btnSignInspection.setVisibility(View.GONE);
            return;
        }
        boolean show = viewModel.shouldShowSignButton(inspection);
        binding.btnSignInspection.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateSignedInfo(InspectionEntity inspection) {
        if (inspection == null || !inspection.signed) {
            binding.cardSignedInfo.setVisibility(View.GONE);
            return;
        }
        binding.cardSignedInfo.setVisibility(View.VISIBLE);
        binding.textSignedBy.setText(getString(R.string.sign_signed_by,
                inspection.signer != null ? inspection.signer : "—"));
        binding.textSignedDate.setText(getString(R.string.sign_signed_date,
                inspection.signDate != null ? inspection.signDate : "—"));
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
        View root = getView();
        if (!isAdded() || root == null) return;
        NavController nav = Navigation.findNavController(root);
        if (nav.getCurrentDestination() != null
                && nav.getCurrentDestination().getId() == R.id.inspectionDetailFragment) {
            Bundle args = new Bundle();
            args.putString("inspectionId", inspection.id);
            args.putString("buildingId", inspection.buildingId != null ? inspection.buildingId : "");
            nav.navigate(R.id.action_inspectionDetail_to_locations, args);
        }
    }

    private void showValidationError(String message) {
        if (message == null || !isAdded()) return;
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
