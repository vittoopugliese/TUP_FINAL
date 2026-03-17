package com.example.tup_final.ui.inspectionlocations;

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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.tup_final.data.entity.LocationWithStats;
import com.example.tup_final.ui.locations.LocationAdapter;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment que muestra las ubicaciones de una inspección.
 * Carga las locations filtradas por buildingId de la inspección seleccionada.
 */
@AndroidEntryPoint
public class InspectionLocationsFragment extends Fragment {

    private static final String ARG_INSPECTION_ID = "inspectionId";
    private static final String ARG_BUILDING_ID = "buildingId";

    private InspectionLocationsViewModel viewModel;
    private LocationAdapter adapter;
    private ProgressBar progressLocations;
    private TextView textEmpty;
    private RecyclerView recyclerLocations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inspection_locations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String inspectionId = getArguments() != null ? getArguments().getString(ARG_INSPECTION_ID, "") : "";
        String buildingId = getArguments() != null ? getArguments().getString(ARG_BUILDING_ID, "") : "";

        viewModel = new ViewModelProvider(this).get(InspectionLocationsViewModel.class);
        adapter = new LocationAdapter();

        recyclerLocations = view.findViewById(R.id.recycler_locations);
        progressLocations = view.findViewById(R.id.progress_locations);
        textEmpty = view.findViewById(R.id.text_empty);

        recyclerLocations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerLocations.setAdapter(adapter);

        adapter.setOnLocationClickListener(item -> {
            Bundle args = new Bundle();
            args.putString("inspectionId", inspectionId);
            args.putString("locationId", item.location.id);
            args.putString("locationName", item.location.name != null ? item.location.name : "");
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_inspection_locations_to_inspection_tests, args);
        });

        viewModel.loadLocations(buildingId);
        observeData();
        setupButtons(view, buildingId, inspectionId);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            NavController nav = NavHostFragment.findNavController(this);
            if (!nav.popBackStack(R.id.inspectionDetailFragment, false)) {
                nav.popBackStack();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null && getArguments() != null) {
            String buildingId = getArguments().getString(ARG_BUILDING_ID, "");
            if (buildingId != null && !buildingId.isEmpty()) {
                viewModel.loadLocations(buildingId);
            }
        }
    }

    private void observeData() {
        viewModel.getLocations().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    progressLocations.setVisibility(View.VISIBLE);
                    textEmpty.setVisibility(View.GONE);
                    recyclerLocations.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    progressLocations.setVisibility(View.GONE);
                    List<LocationWithStats> list = resource.getData();
                    if (list == null || list.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                        textEmpty.setText(R.string.inspection_locations_empty);
                        recyclerLocations.setVisibility(View.GONE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                        recyclerLocations.setVisibility(View.VISIBLE);
                        adapter.submitList(new ArrayList<>(list));
                    }
                    break;
                case ERROR:
                    progressLocations.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.VISIBLE);
                    textEmpty.setText(resource.getMessage() != null
                            ? resource.getMessage() : getString(R.string.locations_error));
                    recyclerLocations.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            resource.getMessage() != null ? resource.getMessage() : getString(R.string.locations_error),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void setupButtons(View view, String buildingId, String inspectionId) {
        view.findViewById(R.id.btn_new_location).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("buildingId", buildingId);
            args.putString("inspectionId", inspectionId);
            NavHostFragment.findNavController(this).navigate(R.id.action_inspection_locations_to_create_location, args);
        });
    }
}
