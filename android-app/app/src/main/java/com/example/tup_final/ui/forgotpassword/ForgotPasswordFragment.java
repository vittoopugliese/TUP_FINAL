package com.example.tup_final.ui.forgotpassword;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.tup_final.R;
import com.example.tup_final.databinding.FragmentForgotPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Pantalla de recuperación de contraseña.
 * Permite al usuario ingresar su email para solicitar un enlace de restablecimiento.
 * Maneja flujo online (API) y offline (BD local).
 */
@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private ForgotPasswordViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.btnSendReset.setOnClickListener(v -> {
            String email = binding.etForgotEmail.getText() != null
                    ? binding.etForgotEmail.getText().toString().trim() : "";
            viewModel.submitEmail(email);
        });

        binding.textBackToLogin.setOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());
    }

    private void observeViewModel() {
        viewModel.getEmailError().observe(getViewLifecycleOwner(), key -> {
            if (key == null) {
                binding.tilForgotEmail.setError(null);
            } else if ("error_email_invalid".equals(key)) {
                binding.tilForgotEmail.setError(getString(R.string.error_email_invalid));
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressForgot.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSendReset.setEnabled(!isLoading);
        });

        viewModel.getResetResult().observe(getViewLifecycleOwner(), result -> {
            binding.textResultMessage.setVisibility(View.VISIBLE);

            switch (result) {
                case SUCCESS_ONLINE:
                    binding.textResultMessage.setText(getString(R.string.forgot_success_online));
                    binding.textResultMessage.setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                    break;
                case SUCCESS_OFFLINE:
                    binding.textResultMessage.setText(getString(R.string.forgot_success_offline));
                    binding.textResultMessage.setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark));
                    break;
                case ERROR_NOT_FOUND:
                    binding.textResultMessage.setText(getString(R.string.forgot_error_not_found));
                    binding.textResultMessage.setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    break;
                case ERROR_NETWORK:
                    binding.textResultMessage.setText(getString(R.string.forgot_error_network));
                    binding.textResultMessage.setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
