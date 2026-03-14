package com.example.tup_final.ui.locations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tup_final.R;
import com.example.tup_final.util.Resource;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment para crear nueva ubicación (T4.1.2).
 */
@AndroidEntryPoint
public class CreateLocationFragment extends Fragment {

    private CreateLocationViewModel viewModel;
    private TextInputLayout layoutName;
    private TextInputEditText inputName;
    private TextInputEditText inputDescription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CreateLocationViewModel.class);

        layoutName = view.findViewById(R.id.layout_name);
        inputName = view.findViewById(R.id.input_name);
        inputDescription = view.findViewById(R.id.input_description);

        String buildingId = getArguments() != null ? getArguments().getString("buildingId", "") : "";

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            viewModel.createLocation(
                    inputName.getText() != null ? inputName.getText().toString() : "",
                    inputDescription.getText() != null ? inputDescription.getText().toString() : "",
                    buildingId != null && !buildingId.isEmpty() ? buildingId : null
            );
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        observeData();
    }

    private void observeData() {
        viewModel.getCreateResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.getStatus() == Resource.Status.SUCCESS) {
                NavHostFragment.findNavController(this).navigateUp();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                String msg = resource.getMessage();
                if ("error_name_duplicate".equals(msg)) {
                    layoutName.setError(getString(R.string.error_name_duplicate));
                    Toast.makeText(requireContext(), R.string.error_name_duplicate, Toast.LENGTH_LONG).show();
                } else {
                    layoutName.setError(null);
                    Toast.makeText(requireContext(),
                            msg != null ? msg : getString(R.string.error_unknown),
                            Toast.LENGTH_SHORT).show();
                }
            } else if (resource.getStatus() == Resource.Status.LOADING) {
                layoutName.setError(null);
            }
        });

        viewModel.getNameError().observe(getViewLifecycleOwner(), errorKey -> {
            if (errorKey != null && "error_name_required".equals(errorKey)) {
                layoutName.setError(getString(R.string.error_name_required));
            } else {
                layoutName.setError(null);
            }
        });
    }
}
