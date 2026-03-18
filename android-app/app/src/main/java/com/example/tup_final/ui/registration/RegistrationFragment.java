package com.example.tup_final.ui.registration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.tup_final.R;
import com.example.tup_final.databinding.FragmentRegistrationBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment de Registro.
 * Muestra formulario nombre, email, contraseña; valida y registra en backend y Room.
 * Tras registro exitoso, navega al Login.
 */
@AndroidEntryPoint
public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding binding;
    private RegistrationViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText() != null
                    ? binding.etName.getText().toString()
                    : "";
            String email = binding.etEmail.getText() != null
                    ? binding.etEmail.getText().toString()
                    : "";
            String password = binding.etPassword.getText() != null
                    ? binding.etPassword.getText().toString()
                    : "";

            viewModel.register(name, email, password);
        });

        viewModel.getRegisterResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressRegister.setVisibility(View.VISIBLE);
                    binding.btnRegister.setEnabled(false);
                    binding.textError.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressRegister.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_register_to_login);

                    break;

                case ERROR:
                    binding.progressRegister.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    binding.textError.setVisibility(View.VISIBLE);
                    binding.textError.setText(resource.getMessage());
                    break;
            }
        });

        viewModel.getNameError().observe(getViewLifecycleOwner(), key -> {
            binding.tilName.setError(key != null ? resolveString(key) : null);
        });
        viewModel.getEmailError().observe(getViewLifecycleOwner(), key -> {
            binding.tilEmail.setError(key != null ? resolveString(key) : null);
        });
        viewModel.getPasswordError().observe(getViewLifecycleOwner(), key -> {
            binding.tilPassword.setError(key != null ? resolveString(key) : null);
        });
    }

    private String resolveString(String key) {
        int resId = getResources().getIdentifier(key, "string", requireContext().getPackageName());
        return resId != 0 ? getString(resId) : key;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
