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
 * Fragment de Login.
 * Muestra formulario email/password, observa el resultado del ViewModel,
 * y navega a HomeFragment tras un login exitoso.
 */
@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Click en botón login
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null
                    ? binding.etEmail.getText().toString()
                    : "";
            String password = binding.etPassword.getText() != null
                    ? binding.etPassword.getText().toString()
                    : "";

            viewModel.login(email, password);
        });

        // Observar resultado del login
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressLogin.setVisibility(View.VISIBLE);
                    binding.btnLogin.setEnabled(false);
                    binding.textError.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressLogin.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    // Navegar a Home, limpiando el login del back stack
                    Navigation.findNavController(view)
                            .navigate(R.id.action_loginFragment_to_homeFragment);
                    break;

                case ERROR:
                    binding.progressLogin.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    binding.textError.setVisibility(View.VISIBLE);
                    binding.textError.setText(resource.getMessage());
                    break;
            }
        });

        binding.textForgotPassword.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_login_to_forgotPassword);
        });

        binding.textRegisterLink.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_login_to_registration);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}