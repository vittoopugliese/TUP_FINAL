package com.example.tup_final.ui.inspection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tup_final.databinding.FragmentDevicesBinding;
import com.example.tup_final.util.Resource;

public class DevicesFragment extends Fragment {

    private FragmentDevicesBinding binding;
    private DeviceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new DeviceAdapter();
        binding.recyclerDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDevices.setAdapter(adapter);

        InspectionDetailViewModel viewModel =
                new ViewModelProvider(requireParentFragment()).get(InspectionDetailViewModel.class);

        viewModel.getInspection().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS
                    && resource.getData() != null
                    && resource.getData().buildingId != null) {
                viewModel.loadDevices(resource.getData().buildingId);
                observeDevices(viewModel);
            }
        });
    }

    private void observeDevices(InspectionDetailViewModel viewModel) {
        if (viewModel.getDevices() == null) return;

        viewModel.getDevices().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressDevices.setVisibility(View.VISIBLE);
                    binding.textEmpty.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressDevices.setVisibility(View.GONE);
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        adapter.submitList(resource.getData());
                        binding.recyclerDevices.setVisibility(View.VISIBLE);
                        binding.textEmpty.setVisibility(View.GONE);
                    } else {
                        binding.recyclerDevices.setVisibility(View.GONE);
                        binding.textEmpty.setVisibility(View.VISIBLE);
                    }
                    break;

                case ERROR:
                    binding.progressDevices.setVisibility(View.GONE);
                    binding.textEmpty.setVisibility(View.VISIBLE);
                    binding.textEmpty.setText(resource.getMessage());
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
