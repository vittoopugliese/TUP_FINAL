package com.example.tup_final.ui.steps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.example.tup_final.data.entity.DeficiencyTypeEntity;
import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.local.UserDao;
import com.example.tup_final.data.repository.DeficiencyTypeRepository;
import com.example.tup_final.util.PhotoMetadata;
import com.example.tup_final.util.Resource;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.widget.ImageView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Bottom Sheet para agregar una Observación o Deficiencia a un Step.
 *
 * Observación (REMARKS):      texto obligatorio, foto opcional.
 * Deficiencia (DEFICIENCIES): texto obligatorio, tipo de deficiencia obligatorio, foto obligatoria.
 *
 * Al confirmar, llama a {@link OnSaveListener#onSave}.
 */
@AndroidEntryPoint
public class AddObservationBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STEP_ID   = "stepId";
    private static final String ARG_STEP_NAME = "stepName";

    // ── Listener ─────────────────────────────────────────────────────────────

    public interface OnSaveListener {
        /**
         * @param stepId           ID del step.
         * @param type             "REMARKS" o "DEFICIENCIES".
         * @param description      Texto de la observación/deficiencia.
         * @param photo            Metadatos de la foto; null si no se adjuntó.
         * @param deficiencyTypeId ID del tipo de deficiencia; null para REMARKS.
         */
        void onSave(String stepId, String type, String description,
                    @Nullable PhotoMetadata photo,
                    @Nullable String deficiencyTypeId);
    }

    // ── Hilt injections ───────────────────────────────────────────────────────

    @Inject SharedPreferences authPrefs;
    @Inject UserDao userDao;
    @Inject DeficiencyTypeRepository deficiencyTypeRepository;

    // ── State ─────────────────────────────────────────────────────────────────

    private String stepId;
    private String stepName;
    private OnSaveListener saveListener;

    /** URI creada para la foto de cámara (necesaria para el launcher TakePicture). */
    private Uri cameraPhotoUri;
    private File cameraImageFile;

    @Nullable private PhotoMetadata currentMetadata;

    /** Catálogo cargado desde Room/API. */
    private List<DeficiencyTypeEntity> deficiencyTypes = new ArrayList<>();
    /** Índice seleccionado en el dropdown (0 = "Seleccionar…"). */
    @Nullable private DeficiencyTypeEntity selectedDeficiencyType;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── Views ────────────────────────────────────────────────────────────────

    private RadioGroup radioGroupType;
    private Chip chipDeficiencyWarning;
    private TextInputLayout tilDeficiencyType;
    private MaterialAutoCompleteTextView spinnerDeficiencyType;
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
                if (Boolean.TRUE.equals(success) && cameraImageFile != null)
                    handlePhotoSelected(Uri.fromFile(cameraImageFile), true);
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (Boolean.TRUE.equals(granted)) launchCamera();
                else Toast.makeText(requireContext(),
                        getString(R.string.obs_camera_permission_denied), Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {});

    // ── Factory ──────────────────────────────────────────────────────────────

    public static AddObservationBottomSheet newInstance(String stepId, String stepName) {
        AddObservationBottomSheet sheet = new AddObservationBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_STEP_ID, stepId);
        args.putString(ARG_STEP_NAME, stepName);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnSaveListener(OnSaveListener listener) { this.saveListener = listener; }

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
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
        loadDeficiencyTypes();
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
        tilDeficiencyType     = root.findViewById(R.id.til_deficiency_type);
        spinnerDeficiencyType = root.findViewById(R.id.spinner_deficiency_type);
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
            tilDeficiencyType.setVisibility(isDeficiency ? View.VISIBLE : View.GONE);
            textPhotoLabel.setText(isDeficiency
                    ? getString(R.string.obs_photo_label_required)
                    : getString(R.string.obs_photo_label_optional));
            textPhotoError.setVisibility(View.GONE);
            tilDeficiencyType.setError(null);
            if (!isDeficiency) selectedDeficiencyType = null;
        });
    }

    private void setupPhotoButtons() {
        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) launchCamera();
            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        });
        btnPickGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnRemovePhoto.setOnClickListener(v -> clearPhoto());
    }

    private void setupSaveCancel() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> trySubmit());
    }

    // ── Deficiency type catalog ────────────────────────────────────────────────

    private void loadDeficiencyTypes() {
        spinnerDeficiencyType.setText(getString(R.string.obs_deficiency_type_loading));
        spinnerDeficiencyType.setEnabled(false);

        androidx.lifecycle.MutableLiveData<Resource<List<DeficiencyTypeEntity>>> liveData =
                new androidx.lifecycle.MutableLiveData<>();

        liveData.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.getStatus() == Resource.Status.LOADING) return;
            liveData.removeObservers(getViewLifecycleOwner());

            List<DeficiencyTypeEntity> types = resource.getData();
            if (types != null && !types.isEmpty()) {
                deficiencyTypes = types;
            }
            populateDeficiencyTypeDropdown();
        });

        deficiencyTypeRepository.loadDeficiencyTypes(liveData);
    }

    private void populateDeficiencyTypeDropdown() {
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.obs_deficiency_type_hint));   // placeholder at [0]
        for (DeficiencyTypeEntity t : deficiencyTypes) {
            names.add(t.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, names);

        spinnerDeficiencyType.setAdapter(adapter);
        spinnerDeficiencyType.setText(names.get(0), false);
        spinnerDeficiencyType.setEnabled(true);

        spinnerDeficiencyType.setOnItemClickListener((parent, view, position, id) -> {
            tilDeficiencyType.setError(null);
            if (position == 0) {
                selectedDeficiencyType = null;
            } else {
                selectedDeficiencyType = deficiencyTypes.get(position - 1);
            }
        });
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
        File dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("OBS_" + ts + "_", ".jpg", dir);
    }

    private void handlePhotoSelected(Uri uri, boolean fromCamera) {
        try {
            String localPath = fromCamera && cameraImageFile != null
                    ? cameraImageFile.getAbsolutePath()
                    : copyUriToLocalFile(uri);

            imagePhotoPreview.setVisibility(View.VISIBLE);
            btnRemovePhoto.setVisibility(View.VISIBLE);
            Glide.with(this).load(uri).centerCrop().into(imagePhotoPreview);
            textPhotoError.setVisibility(View.GONE);

            showMetadataLoading();
            captureMetadataAsync(localPath, Instant.now().toString());

        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.obs_photo_process_error), Toast.LENGTH_SHORT).show();
        }
    }

    private String copyUriToLocalFile(Uri uri) throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File dest = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "OBS_" + ts + ".jpg");
        try (java.io.InputStream in  = requireContext().getContentResolver().openInputStream(uri);
             java.io.OutputStream out = new java.io.FileOutputStream(dest)) {
            if (in == null) throw new IOException("Cannot open input stream");
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
        return dest.getAbsolutePath();
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

    private void showMetadataLoading() {
        cardPhotoMetadata.setVisibility(View.VISIBLE);
        textMetaTimestamp.setText(R.string.meta_loading);
        textMetaGps.setText(R.string.meta_gps_loading);
        textMetaInspector.setText(R.string.meta_loading);
        progressGps.setVisibility(View.VISIBLE);
    }

    private void captureMetadataAsync(String localPath, String timestamp) {
        executor.execute(() -> {
            String inspectorId   = authPrefs.getString("cached_user_id", "");
            String inspectorName = resolveInspectorName(inspectorId);
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

    private String resolveInspectorName(String inspectorId) {
        if (inspectorId != null && !inspectorId.isEmpty()) {
            try {
                UserEntity user = userDao.getById(inspectorId);
                if (user != null) {
                    String full = user.getFullName();
                    if (full != null && !full.trim().isEmpty()) return full.trim();
                }
            } catch (Exception ignored) {}
        }
        return authPrefs.getString("cached_email", getString(R.string.meta_inspector_unknown));
    }

    @SuppressLint("MissingPermission")
    @Nullable
    private Location getLastKnownLocation() {
        boolean hasFine   = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!hasFine && !hasCoarse) {
            mainHandler.post(() -> locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}));
            return null;
        }
        try {
            LocationManager lm = (LocationManager)
                    requireContext().getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) return null;
            for (String p : Arrays.asList(LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)) {
                if (lm.isProviderEnabled(p)) {
                    Location loc = lm.getLastKnownLocation(p);
                    if (loc != null) return loc;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void updateMetadataCard(PhotoMetadata meta, String isoTimestamp) {
        progressGps.setVisibility(View.GONE);
        try {
            java.time.ZonedDateTime zdt = java.time.ZonedDateTime.parse(isoTimestamp)
                    .withZoneSameInstant(java.time.ZoneId.systemDefault());
            textMetaTimestamp.setText(zdt.format(java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm:ss", Locale.getDefault())));
        } catch (Exception e) {
            textMetaTimestamp.setText(isoTimestamp);
        }
        textMetaGps.setText(meta.hasGps() ? meta.formatGps() : getString(R.string.meta_gps_unavailable));
        textMetaInspector.setText(meta.inspectorName != null && !meta.inspectorName.isEmpty()
                ? meta.inspectorName : getString(R.string.meta_inspector_unknown));
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    private void trySubmit() {
        tilDescription.setError(null);
        tilDeficiencyType.setError(null);
        textPhotoError.setVisibility(View.GONE);

        String description = etDescription.getText() != null
                ? etDescription.getText().toString().trim() : "";
        if (description.isEmpty()) {
            tilDescription.setError(getString(R.string.obs_description_required_error));
            return;
        }

        boolean isDeficiency = radioGroupType.getCheckedRadioButtonId() == R.id.radio_deficiency;
        String type = isDeficiency ? "DEFICIENCIES" : "REMARKS";

        if (isDeficiency) {
            if (selectedDeficiencyType == null) {
                tilDeficiencyType.setError(getString(R.string.obs_deficiency_type_required_error));
                return;
            }
            if (currentMetadata == null) {
                textPhotoError.setVisibility(View.VISIBLE);
                return;
            }
        }

        setLoading(true);
        if (saveListener != null) {
            saveListener.onSave(stepId, type, description, currentMetadata,
                    selectedDeficiencyType != null ? selectedDeficiencyType.id : null);
        }
    }

    public void setLoading(boolean loading) {
        btnSave.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
        btnTakePhoto.setEnabled(!loading);
        btnPickGallery.setEnabled(!loading);
        etDescription.setEnabled(!loading);
        radioGroupType.setEnabled(!loading);
        spinnerDeficiencyType.setEnabled(!loading);
        progressObs.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
