package com.example.tup_final.ui.register;

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
import com.example.tup_final.databinding.FragmentRegisterBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment de Registro de usuario.
 * Formulario con email, nombre, contraseña y confirmación.
 * El rol se asigna automáticamente como INSPECTOR.
 * Tras registro exitoso, vuelve al login.
 */
@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        binding.btnRegisterAccept.setOnClickListener(v -> submitRegister());

        binding.textBackToLogin.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        viewModel.getRegisterResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressRegister.setVisibility(View.VISIBLE);
                    binding.btnRegisterAccept.setEnabled(false);
                    binding.textRegisterError.setVisibility(View.GONE);
                    clearFieldErrors();
                    break;

                case SUCCESS:
                    binding.progressRegister.setVisibility(View.GONE);
                    binding.btnRegisterAccept.setEnabled(true);
                    binding.textRegisterError.setVisibility(View.GONE);
                    binding.textRegisterError.setText(null);
                    String email = resource.getData() != null ? resource.getData().getEmail() : null;
                    navigateBackToLogin(email);
                    break;

                case ERROR:
                    binding.progressRegister.setVisibility(View.GONE);
                    binding.btnRegisterAccept.setEnabled(true);
                    binding.textRegisterError.setVisibility(View.VISIBLE);
                    String msg = resource.getMessage();
                    String displayMsg = getStringForError(msg);
                    binding.textRegisterError.setText(displayMsg);
                    break;
            }
        });
    }

    private void submitRegister() {
        String email = getText(binding.etRegisterEmail);
        String fullName = getText(binding.etRegisterName);
        String password = getText(binding.etRegisterPassword);
        String confirmPassword = getText(binding.etRegisterConfirmPassword);

        viewModel.register(email, fullName, password, confirmPassword);
    }

    private String getText(android.widget.EditText et) {
        return et.getText() != null ? et.getText().toString() : "";
    }

    private String getStringForError(String key) {
        if (key == null) return getString(R.string.error_unknown);
        switch (key) {
            case "error_email_invalid":
                return getString(R.string.error_email_invalid);
            case "error_name_required":
                return getString(R.string.error_name_required);
            case "error_password_short":
                return getString(R.string.error_password_short);
            case "error_password_mismatch":
                return getString(R.string.error_password_mismatch);
            default:
                return key;
        }
    }

    private void clearFieldErrors() {
        binding.tilRegisterEmail.setError(null);
        binding.tilRegisterName.setError(null);
        binding.tilRegisterPassword.setError(null);
        binding.tilRegisterConfirmPassword.setError(null);
    }

    private void navigateBackToLogin(@Nullable String email) {
        if (email != null) {
            Bundle result = new Bundle();
            result.putString("email", email);
            getParentFragmentManager().setFragmentResult("register_success", result);
        }
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
