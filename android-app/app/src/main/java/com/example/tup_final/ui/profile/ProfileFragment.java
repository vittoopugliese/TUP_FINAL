package com.example.tup_final.ui.profile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.tup_final.data.repository.AuthRepository;
import com.example.tup_final.ui.home.LogoutDialogFragment;
import com.example.tup_final.sync.SyncScheduler;
import com.example.tup_final.R;
import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.databinding.FragmentProfileBinding;
import com.example.tup_final.util.Resource;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment de Perfil.
 * Muestra los datos del usuario: nombre, email, teléfono, foto de perfil.
 * Soporta modo edición con validación, sync con backend y subida de avatar.
 */
@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject
    AuthRepository authRepository;

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private UserEntity currentUser;
    private Uri cameraPhotoUri;

    // ── ActivityResultLaunchers ──────────────────────────────────────────────

    /** Launcher para seleccionar imagen de la galería */
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            handleSelectedImage(uri);
                        }
                    });

    /** Launcher para tomar foto con la cámara */
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success && cameraPhotoUri != null) {
                            handleSelectedImage(cameraPhotoUri);
                        }
                    });

    /** Launcher para solicitar permiso de cámara */
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            launchCamera();
                        } else {
                            Toast.makeText(requireContext(),
                                    getString(R.string.profile_camera_denied),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

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

        setupLogout();

        // Botón Editar: cambiar a modo edición
        binding.btnEdit.setOnClickListener(v -> switchToEditMode());

        // Botón Cancelar: volver a modo vista
        binding.btnCancel.setOnClickListener(v -> switchToViewMode());

        // Botón Guardar
        binding.btnSave.setOnClickListener(v -> saveProfile());

        // Tocar avatar o badge de cámara para cambiar foto
        binding.imgAvatar.setOnClickListener(v -> showPhotoPickerDialog());
        binding.imgCameraBadge.setOnClickListener(v -> showPhotoPickerDialog());

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

        // Observar resultado de subida de avatar
        viewModel.getUploadResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressProfile.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    binding.progressProfile.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        currentUser = resource.getData();
                        bindUserToView(currentUser);
                    }
                    Toast.makeText(requireContext(),
                            getString(R.string.profile_upload_success),
                            Toast.LENGTH_SHORT).show();
                    break;

                case ERROR:
                    binding.progressProfile.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            resource.getMessage() != null
                                    ? resource.getMessage()
                                    : getString(R.string.profile_upload_error),
                            Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    // ── Logout ─────────────────────────────────────────────────────────────

    private void setupLogout() {
        getParentFragmentManager().setFragmentResultListener(
                LogoutDialogFragment.REQUEST_KEY, this, (key, result) -> {
                    boolean syncFirst = result.getBoolean(LogoutDialogFragment.RESULT_SYNC_FIRST, false);
                    if (syncFirst) {
                        SyncScheduler.enqueueOneTime(requireContext());
                    }
                    Executors.newSingleThreadExecutor().execute(() -> {
                        authRepository.logout();
                        requireActivity().runOnUiThread(() ->
                                NavHostFragment.findNavController(ProfileFragment.this)
                                        .navigate(R.id.action_profile_to_login)
                        );
                    });
                });

        binding.btnLogout.setOnClickListener(v -> {
            boolean hasPendingData = false;
            LogoutDialogFragment.newInstance(hasPendingData)
                    .show(getParentFragmentManager(), "LogoutDialog");
        });
    }

    // ── Photo Picker ────────────────────────────────────────────────────────

    /**
     * Muestra un BottomSheetDialog con opciones: Cámara o Galería.
     */
    private void showPhotoPickerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(16);
        layout.setPadding(pad, pad, pad, pad);

        // Título
        TextView title = new TextView(requireContext());
        title.setText(getString(R.string.profile_change_photo));
        title.setTextSize(18);
        title.setPadding(pad, pad, pad, pad);
        layout.addView(title);

        // Opción: Tomar foto
        TextView optCamera = new TextView(requireContext());
        optCamera.setText(getString(R.string.profile_take_photo));
        optCamera.setTextSize(16);
        optCamera.setPadding(pad, pad * 3 / 4, pad, pad * 3 / 4);
        optCamera.setOnClickListener(v -> {
            dialog.dismiss();
            checkCameraPermissionAndLaunch();
        });
        layout.addView(optCamera);

        // Opción: Elegir de galería
        TextView optGallery = new TextView(requireContext());
        optGallery.setText(getString(R.string.profile_choose_gallery));
        optGallery.setTextSize(16);
        optGallery.setPadding(pad, pad * 3 / 4, pad, pad * 3 / 4);
        optGallery.setOnClickListener(v -> {
            dialog.dismiss();
            galleryLauncher.launch("image/*");
        });
        layout.addView(optGallery);

        dialog.setContentView(layout);
        dialog.show();
    }

    /**
     * Verifica permiso de cámara y lanza la cámara.
     */
    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Crea un archivo temporal y lanza la cámara.
     */
    private void launchCamera() {
        try {
            File photoFile = createTempImageFile();
            cameraPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);
            cameraLauncher.launch(cameraPhotoUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(),
                    getString(R.string.profile_upload_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Crea un archivo temporal para la foto de cámara.
     */
    private File createTempImageFile() throws IOException {
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("avatar_", ".jpg", storageDir);
    }

    /**
     * Procesa la imagen seleccionada: comprime y sube al backend.
     */
    private void handleSelectedImage(Uri imageUri) {
        try {
            File compressedFile = compressImage(imageUri);
            viewModel.uploadAvatar(compressedFile);
        } catch (IOException e) {
            Toast.makeText(requireContext(),
                    getString(R.string.profile_upload_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Comprime la imagen a JPEG con resolución máxima de 512x512
     * y calidad reducida para no superar ~500KB.
     */
    private File compressImage(Uri imageUri) throws IOException {
        InputStream input = requireContext().getContentResolver().openInputStream(imageUri);
        if (input == null) throw new IOException("No se pudo leer la imagen.");

        // Decode con inSampleSize para reducir memoria
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, opts);
        input.close();

        int maxDim = 512;
        int inSampleSize = 1;
        if (opts.outHeight > maxDim || opts.outWidth > maxDim) {
            int halfHeight = opts.outHeight / 2;
            int halfWidth = opts.outWidth / 2;
            while ((halfHeight / inSampleSize) >= maxDim
                    && (halfWidth / inSampleSize) >= maxDim) {
                inSampleSize *= 2;
            }
        }

        opts.inJustDecodeBounds = false;
        opts.inSampleSize = inSampleSize;

        input = requireContext().getContentResolver().openInputStream(imageUri);
        if (input == null) throw new IOException("No se pudo leer la imagen.");
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, opts);
        input.close();

        if (bitmap == null) throw new IOException("No se pudo decodificar la imagen.");

        // Escalar si aún es mayor a maxDim
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > maxDim || height > maxDim) {
            float scale = Math.min((float) maxDim / width, (float) maxDim / height);
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }

        // Guardar comprimido
        File outputFile = new File(requireContext().getCacheDir(), "avatar_compressed.jpg");
        FileOutputStream fos = new FileOutputStream(outputFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        fos.flush();
        fos.close();
        bitmap.recycle();

        return outputFile;
    }

    // ── Binding y navegación ────────────────────────────────────────────────

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
        binding.layoutAvatarContainer.setVisibility(visibility);
        binding.textFullName.setVisibility(visibility);
        binding.textRole.setVisibility(visibility);
        binding.cardEmail.setVisibility(visibility);
        binding.cardPhone.setVisibility(visibility);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
