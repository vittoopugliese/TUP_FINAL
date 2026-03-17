package com.example.tup_final.ui.inspectiontests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.example.tup_final.data.remote.dto.CreateZoneRequest;
import com.example.tup_final.data.remote.dto.DeviceTypeResponse;
import com.example.tup_final.data.remote.dto.MoveDeviceRequest;
import com.example.tup_final.databinding.FragmentInspectionTestsBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    /** Clave para recibir resultado al volver de StepsFragment. */
    public static final String RESULT_KEY_STEPS_COMPLETED = "steps_completed";

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
        adapter.setOnMoveDeviceListener(device -> showMoveDeviceSheet(device, locationId));

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (locationName != null && !locationName.isEmpty()) {
            toolbar.setTitle(locationName);
        }
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        viewModel.loadZones(locationId, inspectionId);
        viewModel.loadDeviceTypes();

        setupStepsResultListener(locationId, inspectionId);
        observeZones();
        observeExpansion();
        observeCreateDevice();
        observeMoveDevice();
        observeCreateZone();

        binding.fabActions.setOnClickListener(v -> showFabActionsSheet(locationId, locationName, inspectionId));
    }

    private AddDeviceBottomSheet addDeviceSheet;
    private AddZoneBottomSheet addZoneSheet;
    private BottomSheetDialog fabActionsSheet;
    private MoveDeviceBottomSheet moveDeviceSheet;

    /** Si true, al completar createZone abrimos AddDeviceBottomSheet con la zona creada. */
    private boolean pendingOpenAddDeviceAfterZone;

    private void showFabActionsSheet(String locationId, String locationName, String inspectionId) {
        if (fabActionsSheet != null) {
            fabActionsSheet.dismiss();
        }
        View root = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_fab_actions, null);
        fabActionsSheet = new BottomSheetDialog(requireContext());
        fabActionsSheet.setContentView(root);

        root.findViewById(R.id.option_create_zone).setOnClickListener(v -> {
            fabActionsSheet.dismiss();
            fabActionsSheet = null;
            openAddZoneSheet(locationId, locationName);
        });

        root.findViewById(R.id.option_add_device).setOnClickListener(v -> {
            fabActionsSheet.dismiss();
            fabActionsSheet = null;
            handleAddDeviceFromFab(locationId, inspectionId);
        });

        fabActionsSheet.show();
    }

    private void openAddZoneSheet(String locationId, String locationName) {
        if (addZoneSheet != null) {
            addZoneSheet.dismiss();
        }
        pendingOpenAddDeviceAfterZone = false;
        addZoneSheet = new AddZoneBottomSheet(requireContext(), locationName, request -> {
            viewModel.createZone(locationId, request);
            if (addZoneSheet != null) addZoneSheet.setLoading(true);
        });
        addZoneSheet.show();
    }

    private void handleAddDeviceFromFab(String locationId, String inspectionId) {
        List<DeviceTypeResponse> types = null;
        Resource<List<DeviceTypeResponse>> typesRes = viewModel.getDeviceTypes().getValue();
        if (typesRes != null && typesRes.getData() != null) {
            types = typesRes.getData();
        }
        if (types == null || types.isEmpty()) {
            Toast.makeText(requireContext(), R.string.device_types_loading, Toast.LENGTH_SHORT).show();
            return;
        }

        List<ZoneUiModel> zones = null;
        Resource<List<ZoneUiModel>> zonesRes = viewModel.getZones().getValue();
        if (zonesRes != null && zonesRes.getData() != null) {
            zones = zonesRes.getData();
        }

        if (zones == null || zones.isEmpty()) {
            pendingOpenAddDeviceAfterZone = true;
            CreateZoneRequest request = new CreateZoneRequest(getString(R.string.auto_zone_name), null);
            viewModel.createZone(locationId, request);
        } else {
            showZonePickerForAddDevice(zones, locationId, inspectionId, types);
        }
    }

    private void showZonePickerForAddDevice(List<ZoneUiModel> zones, String locationId,
                                            String inspectionId, List<DeviceTypeResponse> types) {
        String[] names = new String[zones.size()];
        for (int i = 0; i < zones.size(); i++) {
            names[i] = zones.get(i).name;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_zone_title)
                .setItems(names, (dialog, which) -> {
                    ZoneUiModel zone = zones.get(which);
                    showAddDeviceSheet(zone, locationId, inspectionId, types);
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showMoveDeviceSheet(DeviceUiModel device, String locationId) {
        if (moveDeviceSheet != null) {
            moveDeviceSheet.dismiss();
        }
        List<ZoneUiModel> zones = null;
        Resource<List<ZoneUiModel>> zonesRes = viewModel.getZones().getValue();
        if (zonesRes != null && zonesRes.getData() != null) {
            zones = zonesRes.getData();
        }
        moveDeviceSheet = new MoveDeviceBottomSheet(requireContext(), device, zones, targetZoneId -> {
            viewModel.moveDevice(locationId, device.id, new MoveDeviceRequest(targetZoneId));
            moveDeviceSheet.setLoading(true);
        });
        moveDeviceSheet.show();
    }

    private void showAddDeviceSheet(ZoneUiModel zone, String locationId, String inspectionId) {
        if (addDeviceSheet != null) {
            addDeviceSheet.dismiss();
        }
        List<DeviceTypeResponse> types = null;
        Resource<List<DeviceTypeResponse>> typesRes = viewModel.getDeviceTypes().getValue();
        if (typesRes != null && typesRes.getData() != null) {
            types = typesRes.getData();
        }
        if (types == null || types.isEmpty()) {
            Toast.makeText(requireContext(), R.string.device_types_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        showAddDeviceSheet(zone, locationId, inspectionId, types);
    }

    private void showAddDeviceSheet(ZoneUiModel zone, String locationId, String inspectionId,
                                   List<DeviceTypeResponse> types) {
        if (addDeviceSheet != null) {
            addDeviceSheet.dismiss();
        }
        addDeviceSheet = new AddDeviceBottomSheet(requireContext(), zone, inspectionId, types, request -> {
            viewModel.createDevice(locationId, zone.id, request);
            addDeviceSheet.setLoading(true);
        });
        addDeviceSheet.show();
    }

    private void observeCreateZone() {
        viewModel.getCreateZoneResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (addZoneSheet == null && !pendingOpenAddDeviceAfterZone && resource.getStatus() != LOADING) return;

            switch (resource.getStatus()) {
                case LOADING:
                    if (addZoneSheet != null) addZoneSheet.setLoading(true);
                    break;
                case SUCCESS:
                    ZoneUiModel createdZone = resource.getData();
                    if (addZoneSheet != null) {
                        addZoneSheet.setLoading(false);
                        addZoneSheet.dismiss();
                        addZoneSheet = null;
                    }
                    viewModel.clearCreateZoneResult();
                    Toast.makeText(requireContext(), R.string.zone_created_success, Toast.LENGTH_SHORT).show();
                    viewModel.loadZones(viewModel.getLastLocationId(), viewModel.getLastInspectionId());
                    if (pendingOpenAddDeviceAfterZone && createdZone != null) {
                        pendingOpenAddDeviceAfterZone = false;
                        viewModel.ensureZoneExpanded(createdZone.id);
                        List<DeviceTypeResponse> types = null;
                        Resource<List<DeviceTypeResponse>> typesRes = viewModel.getDeviceTypes().getValue();
                        if (typesRes != null && typesRes.getData() != null) {
                            types = typesRes.getData();
                        }
                        if (types != null && !types.isEmpty()) {
                            showAddDeviceSheet(createdZone, viewModel.getLastLocationId(),
                                    viewModel.getLastInspectionId(), types);
                        }
                    }
                    break;
                case ERROR:
                    pendingOpenAddDeviceAfterZone = false;
                    if (addZoneSheet != null) {
                        addZoneSheet.setLoading(false);
                        addZoneSheet.showError(resource.getMessage());
                    } else {
                        Toast.makeText(requireContext(),
                                resource.getMessage() != null ? resource.getMessage() : getString(R.string.locations_error),
                                Toast.LENGTH_SHORT).show();
                    }
                    viewModel.clearCreateZoneResult();
                    break;
            }
        });
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
                    String deviceIdToExpand = resource.getData() != null ? resource.getData().id : null;
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
                    if (deviceIdToExpand != null) {
                        viewModel.ensureDeviceExpanded(deviceIdToExpand);
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

    private void observeMoveDevice() {
        viewModel.getMoveDeviceResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (moveDeviceSheet == null && resource.getStatus() != LOADING) return;

            switch (resource.getStatus()) {
                case LOADING:
                    if (moveDeviceSheet != null) moveDeviceSheet.setLoading(true);
                    break;
                case SUCCESS:
                    String newZoneId = resource.getData() != null ? resource.getData().getNewZoneId() : null;
                    if (moveDeviceSheet != null) {
                        moveDeviceSheet.setLoading(false);
                        moveDeviceSheet.dismiss();
                        moveDeviceSheet = null;
                    }
                    viewModel.clearMoveDeviceResult();
                    Toast.makeText(requireContext(), R.string.move_device_success, Toast.LENGTH_SHORT).show();
                    if (newZoneId != null) {
                        viewModel.ensureZoneExpanded(newZoneId);
                    }
                    viewModel.loadZones(
                            viewModel.getLastLocationId(),
                            viewModel.getLastInspectionId());
                    break;
                case ERROR:
                    if (moveDeviceSheet != null) {
                        moveDeviceSheet.setLoading(false);
                        moveDeviceSheet.showError(resource.getMessage());
                    }
                    viewModel.clearMoveDeviceResult();
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

    private void setupStepsResultListener(String locationId, String inspectionId) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.setFragmentResultListener(RESULT_KEY_STEPS_COMPLETED, this, (key, bundle) -> {
            viewModel.loadZones(locationId, inspectionId);
        });
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
