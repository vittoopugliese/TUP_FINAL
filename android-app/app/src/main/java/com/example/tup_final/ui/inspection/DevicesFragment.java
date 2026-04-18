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

import com.example.tup_final.R;
import com.example.tup_final.data.entity.DeviceEntity;
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.databinding.FragmentDevicesBinding;
import com.example.tup_final.util.Resource;

import java.util.List;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

public class DevicesFragment extends Fragment {

    private FragmentDevicesBinding binding;
    private DeviceAdapter adapter;
    private boolean devicesLoadTriggered = false;
    /** True si la última carga falló; permite reintentar al volver a la pestaña Devices. */
    private boolean devicesLoadNeedsRetry = false;

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

        viewModel.getDevices().observe(getViewLifecycleOwner(), this::onDevicesChanged);

        viewModel.getInspection().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null
                    && resource.getStatus() == Resource.Status.SUCCESS
                    && resource.getData() != null
                    && !devicesLoadTriggered) {
                InspectionEntity inv = resource.getData();
                String inspectionId = viewModel.getCurrentInspectionId();
                if (inspectionId != null && (inv.locationId != null || inv.buildingId != null)) {
                    devicesLoadTriggered = true;
                    viewModel.loadDevices(inspectionId, inv.locationId, inv.buildingId);
                }
            }
        });
    }

    private void onDevicesChanged(Resource<List<DeviceEntity>> resource) {
        if (resource == null) return;

        switch (resource.getStatus()) {
            case LOADING:
                binding.progressDevices.setVisibility(View.VISIBLE);
                binding.textEmpty.setVisibility(View.GONE);
                binding.recyclerDevices.setVisibility(View.GONE);
                break;

            case SUCCESS:
                binding.progressDevices.setVisibility(View.GONE);
                devicesLoadNeedsRetry = false;
                List<DeviceEntity> data = resource.getData();
                if (data != null && !data.isEmpty()) {
                    adapter.submitList(data);
                    binding.recyclerDevices.setVisibility(View.VISIBLE);
                    binding.textEmpty.setVisibility(View.GONE);
                } else {
                    binding.recyclerDevices.setVisibility(View.GONE);
                    binding.textEmpty.setVisibility(View.VISIBLE);
                    binding.textEmpty.setText(R.string.devices_empty);
                }
                break;

            case ERROR:
                binding.progressDevices.setVisibility(View.GONE);
                binding.recyclerDevices.setVisibility(View.GONE);
                binding.textEmpty.setVisibility(View.VISIBLE);
                binding.textEmpty.setText(resource.getMessage());
                devicesLoadTriggered = false;
                devicesLoadNeedsRetry = true;
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!devicesLoadNeedsRetry || binding == null) {
            return;
        }
        try {
            InspectionDetailViewModel vm = new ViewModelProvider(requireParentFragment())
                    .get(InspectionDetailViewModel.class);
            Resource<InspectionEntity> r = vm.getInspection().getValue();
            if (r != null && r.getStatus() == Resource.Status.SUCCESS && r.getData() != null) {
                InspectionEntity inv = r.getData();
                String inspectionId = vm.getCurrentInspectionId();
                if (inspectionId != null && (inv.locationId != null || inv.buildingId != null)) {
                    devicesLoadNeedsRetry = false;
                    devicesLoadTriggered = true;
                    vm.loadDevices(inspectionId, inv.locationId, inv.buildingId);
                }
            }
        } catch (IllegalStateException ignored) {
            // requireParentFragment no disponible aún
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        devicesLoadTriggered = false;
    }
}
