package com.example.tup_final.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.tup_final.R;
import com.example.tup_final.databinding.FragmentLoginBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Login screen with email and password validation.
 * No backend connection; only UI and validations.
 */
@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        observeErrors();
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void observeErrors() {
        viewModel.getEmailError().observe(getViewLifecycleOwner(), key -> {
            if (key == null) {
                binding.tilEmail.setError(null);
            } else if ("error_email_invalid".equals(key)) {
                binding.tilEmail.setError(getString(R.string.error_email_invalid));
            }
        });
        viewModel.getPasswordError().observe(getViewLifecycleOwner(), key -> {
            if (key == null) {
                binding.tilPassword.setError(null);
            } else if ("error_password_short".equals(key)) {
                binding.tilPassword.setError(getString(R.string.error_password_short));
            }
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        if (viewModel.validate(email, password)) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_login_to_home);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
