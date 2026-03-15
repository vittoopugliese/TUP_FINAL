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
import com.example.tup_final.data.remote.dto.DeviceTypeResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet para agregar un dispositivo a una zona.
 * Usa catálogo de tipos; categoría se deriva del tipo seleccionado.
 */
public class AddDeviceBottomSheet {

    private final BottomSheetDialog dialog;
    private final ZoneUiModel zone;
    private final String inspectionId;
    private final List<DeviceTypeResponse> deviceTypes;
    private final OnSubmitListener listener;

    private TextInputLayout tilName;
    private TextInputLayout tilType;
    private TextInputLayout tilSerial;
    private TextInputEditText etName;
    private AutoCompleteTextView actvType;
    private TextView textDerivedCategory;
    private TextInputEditText etDescription;
    private TextInputEditText etSerial;
    private SwitchMaterial switchEnabled;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private ProgressBar progressBar;

    private DeviceTypeResponse selectedType;

    public interface OnSubmitListener {
        void onSubmit(CreateDeviceRequest request);
    }

    public AddDeviceBottomSheet(Context context, ZoneUiModel zone,
                                String inspectionId,
                                List<DeviceTypeResponse> deviceTypes, OnSubmitListener listener) {
        this.zone = zone;
        this.inspectionId = inspectionId;
        this.deviceTypes = (deviceTypes != null && !deviceTypes.isEmpty())
                ? deviceTypes
                : buildFallbackDeviceTypes();
        this.listener = listener;
        this.dialog = new BottomSheetDialog(context);

        View root = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_add_device, null);
        dialog.setContentView(root);

        tilName = root.findViewById(R.id.til_device_name);
        tilType = root.findViewById(R.id.til_device_type);
        tilSerial = root.findViewById(R.id.til_device_serial);
        etName = root.findViewById(R.id.et_device_name);
        actvType = root.findViewById(R.id.actv_device_type);
        textDerivedCategory = root.findViewById(R.id.text_derived_category);
        etDescription = root.findViewById(R.id.et_device_description);
        etSerial = root.findViewById(R.id.et_device_serial);
        switchEnabled = root.findViewById(R.id.switch_device_enabled);
        btnSave = root.findViewById(R.id.btn_save_device);
        btnCancel = root.findViewById(R.id.btn_cancel);
        progressBar = root.findViewById(R.id.progress_add_device);

        TextView subtitle = root.findViewById(R.id.text_add_device_subtitle);
        subtitle.setText(context.getString(R.string.add_device_subtitle, zone.name));

        String[] typeNames = new String[this.deviceTypes.size()];
        for (int i = 0; i < this.deviceTypes.size(); i++) {
            typeNames[i] = this.deviceTypes.get(i).getName();
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, typeNames);
        actvType.setAdapter(typeAdapter);
        actvType.setKeyListener(null);
        actvType.setOnClickListener(v -> actvType.showDropDown());
        actvType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actvType.showDropDown();
            }
        });
        actvType.setOnItemClickListener((parent, view, position, id) -> {
            selectedType = this.deviceTypes.get(position);
            textDerivedCategory.setVisibility(View.VISIBLE);
            textDerivedCategory.setText(context.getString(R.string.device_derived_category,
                    selectedType.getCategory()));
        });

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
        actvType.setEnabled(!loading);
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
        tilType.setError(null);
        tilSerial.setError(null);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : null;
        String serialStr = etSerial.getText() != null ? etSerial.getText().toString().trim() : null;

        boolean valid = true;
        if (name.isEmpty()) {
            tilName.setError(dialog.getContext().getString(R.string.error_name_required));
            valid = false;
        }
        if (selectedType == null) {
            tilType.setError(dialog.getContext().getString(R.string.device_type_required));
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
        request.setDeviceTypeId(selectedType.getId());
        request.setInspectionId(inspectionId != null && !inspectionId.isEmpty() ? inspectionId : null);
        request.setDescription(description != null && !description.isEmpty() ? description : null);
        request.setSerialNumber(serialNumber);
        request.setEnabled(switchEnabled.isChecked());

        if (listener != null) {
            listener.onSubmit(request);
        }
    }

    public ZoneUiModel getZone() {
        return zone;
    }

    private List<DeviceTypeResponse> buildFallbackDeviceTypes() {
        List<DeviceTypeResponse> list = new ArrayList<>();
        list.add(newType("dt-005", "EXTINGUISHER", "Extintor", "SPRINKLER_DEVICE"));
        list.add(newType("dt-004", "SMOKE_DETECTOR", "Detector de humo", "FA_FIELD_DEVICE"));
        list.add(newType("dt-006", "SPRINKLER_HEAD", "Rociador", "SPRINKLER_DEVICE"));
        list.add(newType("dt-001", "FACP", "Panel de alarma", "FACP_DEVICE"));
        list.add(newType("dt-002", "JOCKEY_PUMP", "Bomba jockey", "JOCKEY_PUMP"));
        list.add(newType("dt-003", "FIRE_PUMP", "Bomba contra incendios", "FIRE_PUMP"));
        return list;
    }

    private DeviceTypeResponse newType(String id, String code, String name, String category) {
        DeviceTypeResponse type = new DeviceTypeResponse();
        type.setId(id);
        type.setCode(code);
        type.setName(name);
        type.setCategory(category);
        type.setEnabled(true);
        return type;
    }
}
