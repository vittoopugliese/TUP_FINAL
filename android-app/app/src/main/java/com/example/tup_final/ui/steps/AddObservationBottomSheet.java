package com.example.tup_final.ui.steps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.tup_final.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Bottom Sheet para agregar una Observación o Deficiencia a un Step.
 *
 * Observación (REMARKS):    texto obligatorio, foto opcional.
 * Deficiencia (DEFICIENCIES): texto obligatorio, foto obligatoria.
 *
 * Al confirmar, llama a {@link OnSaveListener#onSave(String, String, String, String)}.
 */
public class AddObservationBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STEP_ID = "stepId";
    private static final String ARG_STEP_NAME = "stepName";

    public interface OnSaveListener {
        /**
         * @param stepId      ID del step.
         * @param type        "REMARKS" o "DEFICIENCIES".
         * @param description Texto de la observación/deficiencia.
         * @param photoPath   Ruta local de la foto (puede ser null).
         */
        void onSave(String stepId, String type, String description, @Nullable String photoPath);
    }

    private String stepId;
    private String stepName;
    private OnSaveListener saveListener;

    private Uri cameraPhotoUri;
    private String selectedPhotoPath;

    // ── Views ────────────────────────────────────────────────────────────────
    private RadioGroup radioGroupType;
    private Chip chipDeficiencyWarning;
    private TextInputLayout tilDescription;
    private TextInputEditText etDescription;
    private TextView textPhotoLabel;
    private TextView textPhotoError;
    private ImageView imagePhotoPreview;
    private MaterialButton btnRemovePhoto;
    private MaterialButton btnTakePhoto;
    private MaterialButton btnPickGallery;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private View progressObs;

    // ── Activity result launchers ─────────────────────────────────────────────
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) handlePhotoSelected(uri);
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success) && cameraPhotoUri != null) {
                    handlePhotoSelected(cameraPhotoUri);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (Boolean.TRUE.equals(granted)) {
                    launchCamera();
                } else {
                    Toast.makeText(requireContext(),
                            getString(R.string.obs_camera_permission_denied),
                            Toast.LENGTH_SHORT).show();
                }
            });

    // ── Factory ──────────────────────────────────────────────────────────────

    public static AddObservationBottomSheet newInstance(String stepId, String stepName) {
        AddObservationBottomSheet sheet = new AddObservationBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_STEP_ID, stepId);
        args.putString(ARG_STEP_NAME, stepName);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.saveListener = listener;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stepId = getArguments().getString(ARG_STEP_ID, "");
            stepName = getArguments().getString(ARG_STEP_NAME, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_observation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupStepName(view);
        setupTypeSelector();
        setupPhotoButtons();
        setupSaveCancel();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews(View root) {
        radioGroupType = root.findViewById(R.id.radio_group_type);
        chipDeficiencyWarning = root.findViewById(R.id.chip_deficiency_warning);
        tilDescription = root.findViewById(R.id.til_obs_description);
        etDescription = root.findViewById(R.id.et_obs_description);
        textPhotoLabel = root.findViewById(R.id.text_photo_label);
        textPhotoError = root.findViewById(R.id.text_photo_error);
        imagePhotoPreview = root.findViewById(R.id.image_obs_photo_preview);
        btnRemovePhoto = root.findViewById(R.id.btn_remove_photo);
        btnTakePhoto = root.findViewById(R.id.btn_take_photo);
        btnPickGallery = root.findViewById(R.id.btn_pick_gallery);
        btnSave = root.findViewById(R.id.btn_save_obs);
        btnCancel = root.findViewById(R.id.btn_cancel_obs);
        progressObs = root.findViewById(R.id.progress_obs);
    }

    private void setupStepName(View root) {
        TextView textStepName = root.findViewById(R.id.text_obs_step_name);
        if (stepName != null && !stepName.isEmpty()) {
            textStepName.setText(stepName);
            textStepName.setVisibility(View.VISIBLE);
        } else {
            textStepName.setVisibility(View.GONE);
        }
    }

    private void setupTypeSelector() {
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isDeficiency = checkedId == R.id.radio_deficiency;
            chipDeficiencyWarning.setVisibility(isDeficiency ? View.VISIBLE : View.GONE);
            textPhotoLabel.setText(isDeficiency
                    ? getString(R.string.obs_photo_label_required)
                    : getString(R.string.obs_photo_label_optional));
            textPhotoError.setVisibility(View.GONE);
        });
    }

    private void setupPhotoButtons() {
        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnPickGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnRemovePhoto.setOnClickListener(v -> clearPhoto());
    }

    private void setupSaveCancel() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> trySubmit());
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    private void trySubmit() {
        tilDescription.setError(null);
        textPhotoError.setVisibility(View.GONE);

        String description = etDescription.getText() != null
                ? etDescription.getText().toString().trim() : "";
        if (description.isEmpty()) {
            tilDescription.setError(getString(R.string.obs_description_required_error));
            return;
        }

        boolean isDeficiency = radioGroupType.getCheckedRadioButtonId() == R.id.radio_deficiency;
        String type = isDeficiency ? "DEFICIENCIES" : "REMARKS";

        if (isDeficiency && selectedPhotoPath == null) {
            textPhotoError.setVisibility(View.VISIBLE);
            return;
        }

        setLoading(true);
        if (saveListener != null) {
            saveListener.onSave(stepId, type, description, selectedPhotoPath);
        }
    }

    private void handlePhotoSelected(Uri uri) {
        try {
            String path = copyUriToLocalFile(uri);
            selectedPhotoPath = path;
            imagePhotoPreview.setVisibility(View.VISIBLE);
            btnRemovePhoto.setVisibility(View.VISIBLE);
            Glide.with(this).load(uri).centerCrop().into(imagePhotoPreview);
            textPhotoError.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al procesar la foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearPhoto() {
        selectedPhotoPath = null;
        cameraPhotoUri = null;
        imagePhotoPreview.setVisibility(View.GONE);
        btnRemovePhoto.setVisibility(View.GONE);
        imagePhotoPreview.setImageDrawable(null);
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            cameraPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);
            cameraLauncher.launch(cameraPhotoUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error al crear archivo de foto", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "OBS_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private String copyUriToLocalFile(Uri uri) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File destFile = new File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "OBS_" + timeStamp + ".jpg");
        try (java.io.InputStream in = requireContext().getContentResolver().openInputStream(uri);
             java.io.OutputStream out = new java.io.FileOutputStream(destFile)) {
            if (in == null) throw new IOException("Cannot open input stream");
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
        return destFile.getAbsolutePath();
    }

    public void setLoading(boolean loading) {
        btnSave.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
        btnTakePhoto.setEnabled(!loading);
        btnPickGallery.setEnabled(!loading);
        etDescription.setEnabled(!loading);
        radioGroupType.setEnabled(!loading);
        progressObs.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
