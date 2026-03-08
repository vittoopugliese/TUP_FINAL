package com.example.tup_final.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tup_final.R;
import com.example.tup_final.data.repository.AuthRepository;
import com.example.tup_final.sync.SyncScheduler;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

/**
 * Home fragment with logout button and confirmation dialog.
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject
    AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textView = view.findViewById(R.id.text_home);
        textView.setText(R.string.app_name);

        getParentFragmentManager().setFragmentResultListener(
                LogoutDialogFragment.REQUEST_KEY,
                this,
                (requestKey, result) -> {
                    boolean syncFirst = result.getBoolean(LogoutDialogFragment.RESULT_SYNC_FIRST, false);
                    if (syncFirst) {
                        SyncScheduler.enqueueOneTime(requireContext());
                    }
                    authRepository.logout();
                    NavController navController = NavHostFragment.findNavController(this);
                    navController.navigate(R.id.action_home_to_login);
                }
        );

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            boolean hasPendingData = false; // placeholder: no change queue yet
            LogoutDialogFragment dialog = LogoutDialogFragment.newInstance(hasPendingData);
            dialog.show(getParentFragmentManager(), "LogoutDialog");
        });
    }
}
