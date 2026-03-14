package com.example.tup_final.ui.locations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.appbar.MaterialToolbar;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.example.tup_final.data.entity.LocationWithStats;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment para la lista de ubicaciones (T4.1.1).
 */
@AndroidEntryPoint
public class LocationsFragment extends Fragment {

    private LocationsViewModel viewModel;
    private LocationAdapter adapter;
    private ProgressBar progressLocations;
    private TextView textEmpty;
    private RecyclerView recyclerLocations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_locations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LocationsViewModel.class);
        adapter = new LocationAdapter();

        recyclerLocations = view.findViewById(R.id.recycler_locations);
        progressLocations = view.findViewById(R.id.progress_locations);
        textEmpty = view.findViewById(R.id.text_empty);

        recyclerLocations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerLocations.setAdapter(adapter);

        observeData();
        setupButtons(view);
    }

    private void observeData() {
        viewModel.getLocationsResult().observe(getViewLifecycleOwner(), resource -> {
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
                        textEmpty.setText(R.string.locations_empty);
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

    private void setupButtons(View view) {
        view.findViewById(R.id.btn_new_location).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_locations_to_create_location));

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_locations);
        toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }
}
