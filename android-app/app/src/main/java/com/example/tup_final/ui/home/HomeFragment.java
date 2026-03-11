package com.example.tup_final.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.repository.AuthRepository;
import com.example.tup_final.sync.SyncScheduler;
import com.example.tup_final.util.Resource;

import java.util.List;
import java.util.concurrent.Executors;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Home fragment con lista de inspecciones en tarjetas.
 * Muestra: edificio, fecha, estado (con color), días restantes.
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject
    AuthRepository authRepository;

    @Inject
    InspectionDao inspectionDao;

    private HomeViewModel viewModel;
    private InspectionAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private TextView textError;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Vistas
        recyclerView = view.findViewById(R.id.recycler_inspections);
        progressBar = view.findViewById(R.id.progress_inspections);
        textEmpty = view.findViewById(R.id.text_empty);
        textError = view.findViewById(R.id.text_error);

        // RecyclerView setup
        adapter = new InspectionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Botón perfil
        ImageButton btnProfile = view.findViewById(R.id.btn_profile);
        btnProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_home_to_profile));

        // Botón logout
        ImageButton btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            boolean hasPendingData = false;
            LogoutDialogFragment.newInstance(hasPendingData)
                    .show(getParentFragmentManager(), "LogoutDialog");
        });

        // Logout result listener
        viewModel.getInspections().observe(getViewLifecycleOwner(), this::onInspectionsChanged);
        getParentFragmentManager().setFragmentResultListener(
                LogoutDialogFragment.REQUEST_KEY, this, (key, result) -> {
                    boolean syncFirst = result.getBoolean(LogoutDialogFragment.RESULT_SYNC_FIRST, false);
                    if (syncFirst) {
                        SyncScheduler.enqueueOneTime(requireContext());
                    }
                    Executors.newSingleThreadExecutor().execute(() -> {
                        authRepository.logout();
                        requireActivity().runOnUiThread(() ->
                                NavHostFragment.findNavController(HomeFragment.this)
                                        .navigate(R.id.action_home_to_login)
                        );
                    });
                });

        // Observar inspecciones
        viewModel.getInspections().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.GONE);
                    textError.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    textError.setVisibility(View.GONE);

                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        adapter.submitList(resource.getData());
                        recyclerView.setVisibility(View.VISIBLE);
                        textEmpty.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        textEmpty.setVisibility(View.VISIBLE);
                    }
                    break;

                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.GONE);
                    textError.setVisibility(View.VISIBLE);
                    textError.setText(resource.getMessage());
                    break;
            }
        });

        view.findViewById(R.id.btn_profile).setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_home_to_profile));

        view.findViewById(R.id.btn_inspections).setOnClickListener(v ->
                Executors.newSingleThreadExecutor().execute(() -> {
                    List<InspectionEntity> inspections = inspectionDao.getAll();
                    requireActivity().runOnUiThread(() -> {
                        if (inspections != null && !inspections.isEmpty()) {
                            Bundle args = new Bundle();
                            args.putString("inspectionId", inspections.get(0).id);
                            NavHostFragment.findNavController(HomeFragment.this)
                                    .navigate(R.id.action_home_to_inspectionDetail, args);
                        } else {
                            Toast.makeText(requireContext(),
                                    R.string.inspection_no_inspections,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }));
    }

    private void onInspectionsChanged(Resource<List<InspectionEntity>> resource) {
        if (resource == null) return;

        switch (resource.getStatus()) {
            case LOADING:
                progressInspections.setVisibility(View.VISIBLE);
                textEmptyOrError.setVisibility(View.GONE);
                recyclerInspections.setVisibility(View.GONE);
                break;
            case SUCCESS:
                progressInspections.setVisibility(View.GONE);
                List<InspectionEntity> list = resource.getData();
                if (list == null || list.isEmpty()) {
                    textEmptyOrError.setVisibility(View.VISIBLE);
                    textEmptyOrError.setText(R.string.inspections_empty);
                    recyclerInspections.setVisibility(View.GONE);
                } else {
                    textEmptyOrError.setVisibility(View.GONE);
                    recyclerInspections.setVisibility(View.VISIBLE);
                    adapter.setItems(list);
                }
                break;
            case ERROR:
                progressInspections.setVisibility(View.GONE);
                textEmptyOrError.setVisibility(View.VISIBLE);
                textEmptyOrError.setText(resource.getMessage() != null
                        ? resource.getMessage() : getString(R.string.inspections_error));
                recyclerInspections.setVisibility(View.GONE);
                break;
        }
    }
}
