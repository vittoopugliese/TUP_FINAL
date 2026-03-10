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

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment de Perfil.
 * Muestra los datos del usuario: nombre, email, teléfono, foto de perfil.
 * Carga los datos vía ProfileViewModel con fallback offline.
 */
@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

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

        // Observar perfil
        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressProfile.setVisibility(View.VISIBLE);
                    binding.textError.setVisibility(View.GONE);
                    setContentVisibility(View.INVISIBLE);
                    break;

                case SUCCESS:
                    binding.progressProfile.setVisibility(View.GONE);
                    binding.textError.setVisibility(View.GONE);
                    setContentVisibility(View.VISIBLE);

                    if (resource.getData() != null) {
                        UserEntity user = resource.getData();

                        // Nombre completo
                        String fullName = user.getFullName();
                        binding.textFullName.setText(
                                fullName != null && !fullName.isEmpty()
                                        ? fullName
                                        : getString(R.string.profile_no_name));

                        // Rol
                        binding.textRole.setText(
                                user.role != null ? user.role : "");

                        // Email
                        binding.textEmail.setText(
                                user.email != null ? user.email : "—");

                        // Teléfono
                        binding.textPhone.setText(
                                user.phoneNumber != null && !user.phoneNumber.isEmpty()
                                        ? user.phoneNumber
                                        : getString(R.string.profile_no_phone));

                        // Avatar con Glide
                        if (user.avatarImage != null && !user.avatarImage.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(user.avatarImage)
                                    .placeholder(R.drawable.ic_person_placeholder)
                                    .error(R.drawable.ic_person_placeholder)
                                    .circleCrop()
                                    .into(binding.imgAvatar);
                        }
                    }
                    break;

                case ERROR:
                    binding.progressProfile.setVisibility(View.GONE);
                    binding.textError.setVisibility(View.VISIBLE);
                    binding.textError.setText(resource.getMessage());
                    setContentVisibility(View.INVISIBLE);
                    break;
            }
        });
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
