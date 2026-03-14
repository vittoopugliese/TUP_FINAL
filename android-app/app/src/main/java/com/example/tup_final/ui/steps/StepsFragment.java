package com.example.tup_final.ui.steps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tup_final.R;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.navigation.fragment.NavHostFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment que muestra los pasos (steps) de un test.
 * Esqueleto para integrar la lógica de steps según documentación PDS.
 */
@AndroidEntryPoint
public class StepsFragment extends Fragment {

    private static final String ARG_INSPECTION_ID = "inspectionId";
    private static final String ARG_TEST_ID = "testId";
    private static final String ARG_DEVICE_ID = "deviceId";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_steps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String inspectionId = getArguments() != null ? getArguments().getString(ARG_INSPECTION_ID, "") : "";
        String testId = getArguments() != null ? getArguments().getString(ARG_TEST_ID, "") : "";
        String deviceId = getArguments() != null ? getArguments().getString(ARG_DEVICE_ID, "") : "";

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        ProgressBar progress = view.findViewById(R.id.progress_steps);
        progress.setVisibility(View.VISIBLE);

        TextView placeholder = view.findViewById(R.id.text_steps_placeholder);
        if (placeholder != null) {
            placeholder.setVisibility(View.VISIBLE);
            placeholder.setText(getString(R.string.steps_placeholder, testId));
        }
    }
}
