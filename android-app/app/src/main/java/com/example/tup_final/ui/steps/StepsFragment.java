package com.example.tup_final.ui.steps;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
 * Hotfix: evita loading infinito; muestra estado vacío o error según corresponda.
 */
@AndroidEntryPoint
public class StepsFragment extends Fragment {

    private static final String ARG_INSPECTION_ID = "inspectionId";
    private static final String ARG_TEST_ID = "testId";
    private static final String ARG_DEVICE_ID = "deviceId";
    private static final long LOADING_DELAY_MS = 400L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_steps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String testId = getArguments() != null ? getArguments().getString(ARG_TEST_ID, "") : "";

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        ProgressBar progress = view.findViewById(R.id.progress_steps);
        TextView textContent = view.findViewById(R.id.text_steps_content);
        TextView textError = view.findViewById(R.id.text_steps_error);

        if (testId == null || testId.trim().isEmpty()) {
            progress.setVisibility(View.GONE);
            textContent.setVisibility(View.GONE);
            textError.setVisibility(View.VISIBLE);
            textError.setText(R.string.steps_invalid_test);
            return;
        }

        progress.setVisibility(View.VISIBLE);
        textContent.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);

        handler.postDelayed(() -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            textContent.setVisibility(View.VISIBLE);
            textError.setVisibility(View.GONE);
            textContent.setText(R.string.steps_not_available);
        }, LOADING_DELAY_MS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
