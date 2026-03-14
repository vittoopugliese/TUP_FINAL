package com.example.tup_final.ui.inspection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.databinding.FragmentGeneralInfoBinding;

public class GeneralInfoFragment extends Fragment {

    private FragmentGeneralInfoBinding binding;

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

        InspectionDetailViewModel viewModel =
                new ViewModelProvider(requireParentFragment()).get(InspectionDetailViewModel.class);

        viewModel.getInspection().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == com.example.tup_final.util.Resource.Status.SUCCESS
                    && resource.getData() != null) {
                bindInspection(resource.getData());
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
