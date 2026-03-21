package com.example.tup_final.ui.steps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import com.example.tup_final.data.local.UserDao;
import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.util.PhotoMetadata;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Bottom Sheet para agregar una Observación o Deficiencia a un Step.
 *
 * Observación (REMARKS):      texto obligatorio, foto opcional.
 * Deficiencia (DEFICIENCIES): texto obligatorio, foto obligatoria.
 *
 * Al capturar la foto, registra automáticamente:
 *   - Timestamp ISO del momento de captura.
 *   - Coordenadas GPS (si el permiso está concedido).
 *   - Nombre e ID del inspector activo (de SharedPreferences + Room).
 *
 * Al confirmar, llama a {@link OnSaveListener#onSave(String, String, String, PhotoMetadata)}.
 */
@AndroidEntryPoint
public class AddObservationBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STEP_ID   = "stepId";
    private static final String ARG_STEP_NAME = "stepName";

    // ── Listener ─────────────────────────────────────────────────────────────

    public interface OnSaveListener {
        /**
         * @param stepId      ID del step.
         * @param type        "REMARKS" o "DEFICIENCIES".
         * @param description Texto de la observación/deficiencia.
         * @param photo       Metadatos completos de la foto; null si no se adjuntó foto.
         */
        void onSave(String stepId, String type, String description, @Nullable PhotoMetadata photo);
    }

    // ── Hilt injections ───────────────────────────────────────────────────────

    @Inject
    SharedPreferences authPrefs;

    @Inject
    UserDao userDao;

    // ── State ─────────────────────────────────────────────────────────────────

    private String stepId;
    private String stepName;
    private OnSaveListener saveListener;

    /** URI creada para la foto de cámara (necesaria para el launcher TakePicture). */
    private Uri cameraPhotoUri;
    /** Archivo físico donde la cámara guardará la foto (evita re-copia posterior). */
    private File cameraImageFile;

    /**
     * Metadatos de la foto seleccionada/capturada (incluyendo GPS, timestamp, inspector).
     * null hasta que el usuario selecciona una foto.
     */
    @Nullable
    private PhotoMetadata currentMetadata;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

    private MaterialCardView cardPhotoMetadata;
    private TextView textMetaTimestamp;
    private TextView textMetaGps;
    private TextView textMetaInspector;
    private View progressGps;

    // ── Activity result launchers ─────────────────────────────────────────────

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) handlePhotoSelected(uri, false);
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success) && cameraPhotoUri != null) {
                    handlePhotoSelected(cameraPhotoUri, true);
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

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // Permissions were already requested; retry metadata capture if pending
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
            stepId   = getArguments().getString(ARG_STEP_ID, "");
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews(View root) {
        radioGroupType        = root.findViewById(R.id.radio_group_type);
        chipDeficiencyWarning = root.findViewById(R.id.chip_deficiency_warning);
        tilDescription        = root.findViewById(R.id.til_obs_description);
        etDescription         = root.findViewById(R.id.et_obs_description);
        textPhotoLabel        = root.findViewById(R.id.text_photo_label);
        textPhotoError        = root.findViewById(R.id.text_photo_error);
        imagePhotoPreview     = root.findViewById(R.id.image_obs_photo_preview);
        btnRemovePhoto        = root.findViewById(R.id.btn_remove_photo);
        btnTakePhoto          = root.findViewById(R.id.btn_take_photo);
        btnPickGallery        = root.findViewById(R.id.btn_pick_gallery);
        btnSave               = root.findViewById(R.id.btn_save_obs);
        btnCancel             = root.findViewById(R.id.btn_cancel_obs);
        progressObs           = root.findViewById(R.id.progress_obs);
        cardPhotoMetadata     = root.findViewById(R.id.card_photo_metadata);
        textMetaTimestamp     = root.findViewById(R.id.text_meta_timestamp);
        textMetaGps           = root.findViewById(R.id.text_meta_gps);
        textMetaInspector     = root.findViewById(R.id.text_meta_inspector);
        progressGps           = root.findViewById(R.id.progress_gps);
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

    // ── Photo handling ────────────────────────────────────────────────────────

    private void launchCamera() {
        try {
            cameraImageFile = createImageFile();
            cameraPhotoUri  = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    cameraImageFile);
            cameraLauncher.launch(cameraPhotoUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), getString(R.string.obs_photo_create_error), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("OBS_" + ts + "_", ".jpg", storageDir);
    }

    /**
     * Called once a photo is captured (camera) or selected (gallery).
     * For camera photos, reuses the existing temp file path directly.
     * For gallery photos, copies the content URI to a local file first.
     *
     * @param uri         The photo URI (content:// or file://).
     * @param fromCamera  True if taken with camera (file already saved); false if from gallery.
     */
    private void handlePhotoSelected(Uri uri, boolean fromCamera) {
        try {
            String localPath;
            if (fromCamera && cameraImageFile != null) {
                // Camera already wrote to cameraImageFile; reuse path directly
                localPath = cameraImageFile.getAbsolutePath();
            } else {
                localPath = copyUriToLocalFile(uri);
            }

            // Show photo preview immediately
            imagePhotoPreview.setVisibility(View.VISIBLE);
            btnRemovePhoto.setVisibility(View.VISIBLE);
            Glide.with(this).load(uri).centerCrop().into(imagePhotoPreview);
            textPhotoError.setVisibility(View.GONE);

            // Show metadata card with loading state while we resolve GPS + user info
            showMetadataLoading();

            // Capture all metadata asynchronously
            final String capturedPath = localPath;
            final String capturedTimestamp = Instant.now().toString();
            captureMetadataAsync(capturedPath, capturedTimestamp);

        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.obs_photo_process_error), Toast.LENGTH_SHORT).show();
        }
    }

    private String copyUriToLocalFile(Uri uri) throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File destFile = new File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "OBS_" + ts + ".jpg");
        try (java.io.InputStream in  = requireContext().getContentResolver().openInputStream(uri);
             java.io.OutputStream out = new java.io.FileOutputStream(destFile)) {
            if (in == null) throw new IOException("Cannot open input stream");
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
        return destFile.getAbsolutePath();
    }

    private void clearPhoto() {
        currentMetadata = null;
        cameraPhotoUri  = null;
        cameraImageFile = null;
        imagePhotoPreview.setVisibility(View.GONE);
        btnRemovePhoto.setVisibility(View.GONE);
        cardPhotoMetadata.setVisibility(View.GONE);
        imagePhotoPreview.setImageDrawable(null);
    }

    // ── Metadata capture ──────────────────────────────────────────────────────

    /** Shows metadata card in "loading" state while GPS + user are resolved. */
    private void showMetadataLoading() {
        cardPhotoMetadata.setVisibility(View.VISIBLE);
        textMetaTimestamp.setText(R.string.meta_loading);
        textMetaGps.setText(R.string.meta_gps_loading);
        textMetaInspector.setText(R.string.meta_loading);
        progressGps.setVisibility(View.VISIBLE);
    }

    /**
     * Fetches GPS location and inspector info on a background thread, then
     * constructs PhotoMetadata and updates the metadata card on the main thread.
     */
    private void captureMetadataAsync(String localPath, String timestamp) {
        executor.execute(() -> {
            // 1. Get inspector info from SharedPrefs + Room
            String inspectorId   = authPrefs.getString("cached_user_id", "");
            String inspectorName = resolveInspectorName(inspectorId);

            // 2. Get GPS location (last known - fast and works offline)
            Location gpsLocation = getLastKnownLocation();

            Double lat = gpsLocation != null ? gpsLocation.getLatitude()  : null;
            Double lon = gpsLocation != null ? gpsLocation.getLongitude() : null;

            PhotoMetadata meta = new PhotoMetadata(localPath, timestamp, lat, lon,
                    inspectorId, inspectorName);

            mainHandler.post(() -> {
                currentMetadata = meta;
                updateMetadataCard(meta, timestamp);
            });
        });
    }

    /** Queries UserEntity from Room by ID; falls back to cached email if not found. */
    private String resolveInspectorName(String inspectorId) {
        if (inspectorId != null && !inspectorId.isEmpty()) {
            try {
                UserEntity user = userDao.getById(inspectorId);
                if (user != null) {
                    String fullName = user.getFullName();
                    if (fullName != null && !fullName.trim().isEmpty()) {
                        return fullName.trim();
                    }
                }
            } catch (Exception ignored) {}
        }
        // Fallback: use cached email
        return authPrefs.getString("cached_email", getString(R.string.meta_inspector_unknown));
    }

    /**
     * Tries to get the last known location from GPS, NETWORK, and PASSIVE providers.
     * This is synchronous and returns immediately (no new fix requested).
     */
    @SuppressLint("MissingPermission")
    @Nullable
    private Location getLastKnownLocation() {
        boolean hasFine = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!hasFine && !hasCoarse) {
            // Request permissions for next time; GPS will be null this round
            mainHandler.post(() -> locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }));
            return null;
        }

        try {
            LocationManager lm = (LocationManager)
                    requireContext().getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) return null;

            List<String> providers = Arrays.asList(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER);

            for (String provider : providers) {
                if (lm.isProviderEnabled(provider)) {
                    Location loc = lm.getLastKnownLocation(provider);
                    if (loc != null) return loc;
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    /** Populates the metadata card with resolved values. */
    private void updateMetadataCard(PhotoMetadata meta, String isoTimestamp) {
        progressGps.setVisibility(View.GONE);

        // Format timestamp for display
        try {
            java.time.ZonedDateTime zdt = java.time.ZonedDateTime.parse(isoTimestamp)
                    .withZoneSameInstant(java.time.ZoneId.systemDefault());
            String formatted = zdt.format(java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm:ss", Locale.getDefault()));
            textMetaTimestamp.setText(formatted);
        } catch (Exception e) {
            textMetaTimestamp.setText(isoTimestamp);
        }

        // GPS
        String gpsText = meta.hasGps()
                ? meta.formatGps()
                : getString(R.string.meta_gps_unavailable);
        textMetaGps.setText(gpsText);

        // Inspector
        textMetaInspector.setText(meta.inspectorName != null && !meta.inspectorName.isEmpty()
                ? meta.inspectorName
                : getString(R.string.meta_inspector_unknown));
    }

    // ── Submit ────────────────────────────────────────────────────────────────

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

        if (isDeficiency && currentMetadata == null) {
            textPhotoError.setVisibility(View.VISIBLE);
            return;
        }

        setLoading(true);
        if (saveListener != null) {
            saveListener.onSave(stepId, type, description, currentMetadata);
        }
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
