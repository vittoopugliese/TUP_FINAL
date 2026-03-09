package com.example.tup_final.ui.home;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tup_final.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Confirmation dialog for logout. Shows different messages depending on
 * whether there is pending data to sync.
 */
public class LogoutDialogFragment extends DialogFragment {

    private static final String ARG_HAS_PENDING_DATA = "has_pending_data";
    public static final String REQUEST_KEY = "logout_result";
    public static final String RESULT_SYNC_FIRST = "sync_first";

    public static LogoutDialogFragment newInstance(boolean hasPendingData) {
        LogoutDialogFragment fragment = new LogoutDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HAS_PENDING_DATA, hasPendingData);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        boolean hasPendingData = getArguments() != null
                && getArguments().getBoolean(ARG_HAS_PENDING_DATA, false);

        String message = hasPendingData
                ? getString(R.string.logout_pending_message)
                : getString(R.string.logout_message);
        String positiveText = hasPendingData
                ? getString(R.string.btn_sync_and_logout)
                : getString(R.string.btn_logout);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_title)
                .setMessage(message)
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dismiss())
                .setPositiveButton(positiveText, (dialog, which) -> {
                    Bundle result = new Bundle();
                    result.putBoolean(RESULT_SYNC_FIRST, hasPendingData);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                })
                .create();
    }
}
