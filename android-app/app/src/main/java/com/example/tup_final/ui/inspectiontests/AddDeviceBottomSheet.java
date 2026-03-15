package com.example.tup_final.ui.inspectiontests;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tup_final.R;
import com.example.tup_final.data.remote.dto.CreateDeviceRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Bottom sheet para agregar un dispositivo a una zona.
 */
public class AddDeviceBottomSheet {

    private static final String[] DEVICE_CATEGORIES = {
            "FA_FIELD_DEVICE",
            "FACP_DEVICE",
            "JOCKEY_PUMP",
            "FIRE_PUMP",
            "PUMP_CONTROLLER",
            "SPRINKLER_DEVICE"
    };

    private final BottomSheetDialog dialog;
    private final ZoneUiModel zone;
    private final OnSubmitListener listener;

    private TextInputLayout tilName;
    private TextInputLayout tilCategory;
    private TextInputLayout tilSerial;
    private TextInputEditText etName;
    private AutoCompleteTextView actvCategory;
    private TextInputEditText etDescription;
    private TextInputEditText etSerial;
    private SwitchMaterial switchEnabled;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private ProgressBar progressBar;

    public interface OnSubmitListener {
        void onSubmit(CreateDeviceRequest request);
    }

    public AddDeviceBottomSheet(Context context, ZoneUiModel zone, OnSubmitListener listener) {
        this.zone = zone;
        this.listener = listener;
        this.dialog = new BottomSheetDialog(context);

        View root = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_add_device, null);
        dialog.setContentView(root);

        tilName = root.findViewById(R.id.til_device_name);
        tilCategory = root.findViewById(R.id.til_device_category);
        tilSerial = root.findViewById(R.id.til_device_serial);
        etName = root.findViewById(R.id.et_device_name);
        actvCategory = root.findViewById(R.id.actv_device_category);
        etDescription = root.findViewById(R.id.et_device_description);
        etSerial = root.findViewById(R.id.et_device_serial);
        switchEnabled = root.findViewById(R.id.switch_device_enabled);
        btnSave = root.findViewById(R.id.btn_save_device);
        btnCancel = root.findViewById(R.id.btn_cancel);
        progressBar = root.findViewById(R.id.progress_add_device);

        TextView subtitle = root.findViewById(R.id.text_add_device_subtitle);
        subtitle.setText(context.getString(R.string.add_device_subtitle, zone.name));

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, DEVICE_CATEGORIES);
        actvCategory.setAdapter(categoryAdapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> trySubmit());
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setLoading(boolean loading) {
        btnSave.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
        etName.setEnabled(!loading);
        actvCategory.setEnabled(!loading);
        etDescription.setEnabled(!loading);
        etSerial.setEnabled(!loading);
        switchEnabled.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    public void showError(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(dialog.getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void trySubmit() {
        tilName.setError(null);
        tilCategory.setError(null);
        tilSerial.setError(null);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String category = actvCategory.getText() != null ? actvCategory.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : null;
        String serialStr = etSerial.getText() != null ? etSerial.getText().toString().trim() : null;

        boolean valid = true;
        if (name.isEmpty()) {
            tilName.setError(dialog.getContext().getString(R.string.error_name_required));
            valid = false;
        }
        if (category.isEmpty()) {
            tilCategory.setError(dialog.getContext().getString(R.string.device_category_required));
            valid = false;
        }

        Integer serialNumber = null;
        if (serialStr != null && !serialStr.isEmpty()) {
            try {
                serialNumber = Integer.parseInt(serialStr);
            } catch (NumberFormatException e) {
                tilSerial.setError(dialog.getContext().getString(R.string.device_serial_invalid));
                valid = false;
            }
        }

        if (!valid) return;

        CreateDeviceRequest request = new CreateDeviceRequest();
        request.setName(name);
        request.setDeviceCategory(category);
        request.setDescription(description.isEmpty() ? null : description);
        request.setSerialNumber(serialNumber);
        request.setEnabled(switchEnabled.isChecked());

        if (listener != null) {
            listener.onSubmit(request);
        }
    }

    public ZoneUiModel getZone() {
        return zone;
    }
}
