package com.example.tup_final.ui.inspectiontests;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.example.tup_final.R;
import com.example.tup_final.data.remote.dto.CreateZoneRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Bottom sheet para crear una zona en una ubicación.
 */
public class AddZoneBottomSheet {

    private final BottomSheetDialog dialog;
    private final String locationName;
    private final OnSubmitListener listener;

    private TextInputLayout tilName;
    private TextInputEditText etName;
    private TextInputEditText etDetails;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private View progressBar;

    public interface OnSubmitListener {
        void onSubmit(CreateZoneRequest request);
    }

    public AddZoneBottomSheet(Context context, String locationName, OnSubmitListener listener) {
        this.locationName = locationName != null ? locationName : "";
        this.listener = listener;
        this.dialog = new BottomSheetDialog(context);

        View root = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_add_zone, null);
        dialog.setContentView(root);

        tilName = root.findViewById(R.id.til_zone_name);
        etName = root.findViewById(R.id.et_zone_name);
        etDetails = root.findViewById(R.id.et_zone_details);
        btnSave = root.findViewById(R.id.btn_save_zone);
        btnCancel = root.findViewById(R.id.btn_cancel_zone);
        progressBar = root.findViewById(R.id.progress_add_zone);

        android.widget.TextView subtitle = root.findViewById(R.id.text_create_zone_subtitle);
        subtitle.setText(context.getString(R.string.create_zone_subtitle, this.locationName));

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
        etDetails.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    public void showError(String message) {
        if (message != null && !message.isEmpty()) {
            android.widget.Toast.makeText(dialog.getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void trySubmit() {
        tilName.setError(null);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String details = etDetails.getText() != null ? etDetails.getText().toString().trim() : null;

        if (name.isEmpty()) {
            tilName.setError(dialog.getContext().getString(R.string.error_zone_name_required));
            return;
        }

        CreateZoneRequest request = new CreateZoneRequest();
        request.setName(name);
        request.setDetails(details != null && !details.isEmpty() ? details : null);

        if (listener != null) {
            listener.onSubmit(request);
        }
    }
}
