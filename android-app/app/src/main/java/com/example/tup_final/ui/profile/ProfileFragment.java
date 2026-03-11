package com.example.tup_final.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.tup_final.R;
import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.databinding.FragmentProfileBinding;
import com.example.tup_final.util.Resource;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment de Perfil.
 * Muestra los datos del usuario: nombre, email, teléfono, foto de perfil.
 * Soporta modo edición con validación y sync con backend.
 */
@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private UserEntity currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Botón volver
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        // Botón Editar: cambiar a modo edición
        binding.btnEdit.setOnClickListener(v -> switchToEditMode());

        // Botón Cancelar: volver a modo vista
        binding.btnCancel.setOnClickListener(v -> switchToViewMode());

        // Botón Guardar
        binding.btnSave.setOnClickListener(v -> saveProfile());

        // Observar perfil
        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressProfile.setVisibility(View.VISIBLE);
                    binding.textError.setVisibility(View.GONE);
                    setContentVisibility(View.INVISIBLE);
                    binding.btnEdit.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressProfile.setVisibility(View.GONE);
                    binding.textError.setVisibility(View.GONE);
                    setContentVisibility(View.VISIBLE);
                    binding.btnEdit.setVisibility(View.VISIBLE);

                    if (resource.getData() != null) {
                        currentUser = resource.getData();
                        bindUserToView(currentUser);
                    }
                    break;

                case ERROR:
                    binding.progressProfile.setVisibility(View.GONE);
                    binding.textError.setVisibility(View.VISIBLE);
                    binding.textError.setText(resource.getMessage());
                    setContentVisibility(View.INVISIBLE);
                    binding.btnEdit.setVisibility(View.GONE);
                    break;
            }
        });

        // Observar resultado del guardado
        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    binding.btnSave.setEnabled(false);
                    binding.btnCancel.setEnabled(false);
                    binding.tilFirstName.setError(null);
                    binding.tilLastName.setError(null);
                    binding.tilPhone.setError(null);
                    binding.textError.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.btnSave.setEnabled(true);
                    binding.btnCancel.setEnabled(true);
                    if (resource.getData() != null) {
                        currentUser = resource.getData();
                        bindUserToView(currentUser);
                        switchToViewMode();
                    }
                    binding.textError.setVisibility(View.GONE);
                    break;

                case ERROR:
                    binding.btnSave.setEnabled(true);
                    binding.btnCancel.setEnabled(true);
                    String msg = resource.getMessage();
                    if (msg != null) {
                        if ("error_name_required".equals(msg)) {
                            binding.tilFirstName.setError(getString(R.string.error_name_required));
                        } else if ("error_lastname_required".equals(msg)) {
                            binding.tilLastName.setError(getString(R.string.error_lastname_required));
                        } else if ("error_phone_invalid".equals(msg)) {
                            binding.tilPhone.setError(getString(R.string.error_phone_invalid));
                        } else {
                            binding.textError.setVisibility(View.VISIBLE);
                            binding.textError.setText(msg);
                        }
                    }
                    break;
            }
        });
    }

    private void bindUserToView(UserEntity user) {
        String fullName = user.getFullName();
        binding.textFullName.setText(
                fullName != null && !fullName.isEmpty()
                        ? fullName
                        : getString(R.string.profile_no_name));

        binding.textRole.setText(user.role != null ? user.role : "");
        binding.textEmail.setText(user.email != null ? user.email : "—");
        binding.textPhone.setText(
                user.phoneNumber != null && !user.phoneNumber.isEmpty()
                        ? user.phoneNumber
                        : getString(R.string.profile_no_phone));

        if (user.avatarImage != null && !user.avatarImage.isEmpty()) {
            Glide.with(requireContext())
                    .load(user.avatarImage)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(binding.imgAvatar);
        }
    }

    private void switchToEditMode() {
        if (currentUser == null) return;

        binding.textFullName.setVisibility(View.GONE);
        binding.textRole.setVisibility(View.GONE);
        binding.cardPhone.setVisibility(View.GONE);
        binding.btnEdit.setVisibility(View.GONE);

        binding.etFirstName.setText(currentUser.firstName != null ? currentUser.firstName : "");
        binding.etLastName.setText(currentUser.lastName != null ? currentUser.lastName : "");
        binding.etPhone.setText(currentUser.phoneNumber != null ? currentUser.phoneNumber : "");

        binding.layoutEdit.setVisibility(View.VISIBLE);
    }

    private void switchToViewMode() {
        binding.layoutEdit.setVisibility(View.GONE);

        binding.textFullName.setVisibility(View.VISIBLE);
        binding.textRole.setVisibility(View.VISIBLE);
        binding.cardPhone.setVisibility(View.VISIBLE);
        binding.btnEdit.setVisibility(View.VISIBLE);

        binding.tilFirstName.setError(null);
        binding.tilLastName.setError(null);
        binding.tilPhone.setError(null);
    }

    private void saveProfile() {
        String firstName = binding.etFirstName.getText() != null
                ? binding.etFirstName.getText().toString() : "";
        String lastName = binding.etLastName.getText() != null
                ? binding.etLastName.getText().toString() : "";
        String phone = binding.etPhone.getText() != null
                ? binding.etPhone.getText().toString() : "";

        viewModel.updateProfile(firstName, lastName, phone);
    }

    /**
     * Muestra u oculta el contenido principal del perfil.
     */
    private void setContentVisibility(int visibility) {
        binding.imgAvatar.setVisibility(visibility);
        binding.textFullName.setVisibility(visibility);
        binding.textRole.setVisibility(visibility);
        binding.cardEmail.setVisibility(visibility);
        binding.cardPhone.setVisibility(visibility);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
