package com.example.tup_final.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
 * Home fragment with inspections list, logout and profile buttons.
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject
    AuthRepository authRepository;

    private HomeViewModel viewModel;
    private InspectionAdapter adapter;
    private RecyclerView recyclerInspections;
    private ProgressBar progressInspections;
    private TextView textEmptyOrError;

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

        recyclerInspections = view.findViewById(R.id.recycler_inspections);
        progressInspections = view.findViewById(R.id.progress_inspections);
        textEmptyOrError = view.findViewById(R.id.text_inspections_empty_or_error);

        adapter = new InspectionAdapter();
        recyclerInspections.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerInspections.setAdapter(adapter);

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

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            boolean hasPendingData = false; // placeholder: no change queue yet
            LogoutDialogFragment.newInstance(hasPendingData)
                    .show(getParentFragmentManager(), "LogoutDialog");
        });

        view.findViewById(R.id.btn_profile).setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_home_to_profile));
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
