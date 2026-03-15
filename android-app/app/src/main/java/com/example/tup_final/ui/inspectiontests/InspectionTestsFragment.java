package com.example.tup_final.ui.inspectiontests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.tup_final.databinding.FragmentInspectionTestsBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

@AndroidEntryPoint
public class InspectionTestsFragment extends Fragment {

    private static final String ARG_INSPECTION_ID = "inspectionId";
    private static final String ARG_LOCATION_ID = "locationId";
    private static final String ARG_LOCATION_NAME = "locationName";

    private InspectionTestsViewModel viewModel;
    private ZonesDevicesTestsAdapter adapter;
    private FragmentInspectionTestsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInspectionTestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String inspectionId = getArguments() != null ? getArguments().getString(ARG_INSPECTION_ID, "") : "";
        String locationId = getArguments() != null ? getArguments().getString(ARG_LOCATION_ID, "") : "";
        String locationName = getArguments() != null ? getArguments().getString(ARG_LOCATION_NAME, "") : "";

        viewModel = new ViewModelProvider(this).get(InspectionTestsViewModel.class);
        adapter = new ZonesDevicesTestsAdapter();

        binding.recyclerZonesDevicesTests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerZonesDevicesTests.setAdapter(adapter);

        adapter.setOnZoneExpandListener(zoneId -> viewModel.toggleZoneExpanded(zoneId));
        adapter.setOnDeviceExpandListener(deviceId -> viewModel.toggleDeviceExpanded(deviceId));
        adapter.setOnTestClickListener(test -> navigateToSteps(inspectionId, test.id, test.deviceId));
        adapter.setOnAddDeviceListener(zone -> showAddDeviceSheet(zone, locationId, inspectionId));

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (locationName != null && !locationName.isEmpty()) {
            toolbar.setTitle(locationName);
        }
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        viewModel.loadZones(locationId, inspectionId);

        observeZones();
        observeExpansion();
        observeCreateDevice();
    }

    private AddDeviceBottomSheet addDeviceSheet;

    private void showAddDeviceSheet(ZoneUiModel zone, String locationId, String inspectionId) {
        if (addDeviceSheet != null) {
            addDeviceSheet.dismiss();
        }
        addDeviceSheet = new AddDeviceBottomSheet(requireContext(), zone, request -> {
            viewModel.createDevice(locationId, zone.id, request);
            addDeviceSheet.setLoading(true);
        });
        addDeviceSheet.show();
    }

    private void observeCreateDevice() {
        viewModel.getCreateDeviceResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (addDeviceSheet == null && resource.getStatus() != LOADING) return;

            switch (resource.getStatus()) {
                case LOADING:
                    if (addDeviceSheet != null) addDeviceSheet.setLoading(true);
                    break;
                case SUCCESS:
                    String zoneIdToExpand = addDeviceSheet != null ? addDeviceSheet.getZone().id : null;
                    if (addDeviceSheet != null) {
                        addDeviceSheet.setLoading(false);
                        addDeviceSheet.dismiss();
                        addDeviceSheet = null;
                    }
                    viewModel.clearCreateDeviceResult();
                    Toast.makeText(requireContext(), R.string.device_created_success, Toast.LENGTH_SHORT).show();
                    if (zoneIdToExpand != null) {
                        viewModel.ensureZoneExpanded(zoneIdToExpand);
                    }
                    viewModel.loadZones(
                            viewModel.getLastLocationId(),
                            viewModel.getLastInspectionId());
                    break;
                case ERROR:
                    if (addDeviceSheet != null) {
                        addDeviceSheet.setLoading(false);
                        addDeviceSheet.showError(resource.getMessage());
                    }
                    viewModel.clearCreateDeviceResult();
                    break;
            }
        });
    }

    private void observeZones() {
        viewModel.getZones().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressTests.setVisibility(View.VISIBLE);
                    binding.textEmpty.setVisibility(View.GONE);
                    binding.recyclerZonesDevicesTests.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressTests.setVisibility(View.GONE);
                    List<ZoneUiModel> zones = resource.getData();
                    if (zones == null || zones.isEmpty()) {
                        binding.textEmpty.setVisibility(View.VISIBLE);
                        binding.textEmpty.setText(R.string.inspection_tests_empty);
                        binding.recyclerZonesDevicesTests.setVisibility(View.GONE);
                    } else {
                        binding.textEmpty.setVisibility(View.GONE);
                        binding.recyclerZonesDevicesTests.setVisibility(View.VISIBLE);
                        updateAdapterList(zones);
                    }
                    break;
                case ERROR:
                    binding.progressTests.setVisibility(View.GONE);
                    binding.textEmpty.setVisibility(View.VISIBLE);
                    binding.textEmpty.setText(resource.getMessage() != null
                            ? resource.getMessage() : getString(R.string.locations_error));
                    binding.recyclerZonesDevicesTests.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            resource.getMessage() != null ? resource.getMessage() : getString(R.string.locations_error),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void observeExpansion() {
        viewModel.getExpandedZoneIds().observe(getViewLifecycleOwner(), zoneIds -> {
            Set<String> deviceIds = viewModel.getExpandedDeviceIds().getValue();
            adapter.setExpandedState(zoneIds != null ? zoneIds : new HashSet<>(),
                    deviceIds != null ? deviceIds : new HashSet<>());
            Resource<List<ZoneUiModel>> zonesRes = viewModel.getZones().getValue();
            if (zonesRes != null && zonesRes.getData() != null) {
                updateAdapterList(zonesRes.getData());
            }
        });
        viewModel.getExpandedDeviceIds().observe(getViewLifecycleOwner(), deviceIds -> {
            Set<String> zoneIds = viewModel.getExpandedZoneIds().getValue();
            adapter.setExpandedState(zoneIds != null ? zoneIds : new HashSet<>(),
                    deviceIds != null ? deviceIds : new HashSet<>());
            Resource<List<ZoneUiModel>> zonesRes = viewModel.getZones().getValue();
            if (zonesRes != null && zonesRes.getData() != null) {
                updateAdapterList(zonesRes.getData());
            }
        });
    }

    private void updateAdapterList(List<ZoneUiModel> zones) {
        Set<String> zoneIds = viewModel.getExpandedZoneIds().getValue();
        Set<String> deviceIds = viewModel.getExpandedDeviceIds().getValue();
        List<ZonesDevicesTestsAdapter.ListItem> items = ZonesDevicesTestsAdapter.buildFlatList(
                zones, zoneIds, deviceIds);
        adapter.submitList(new ArrayList<>(items));
    }

    private void navigateToSteps(String inspectionId, String testId, String deviceId) {
        Bundle args = new Bundle();
        args.putString("inspectionId", inspectionId);
        args.putString("testId", testId);
        args.putString("deviceId", deviceId != null ? deviceId : "");
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_inspection_tests_to_steps, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
