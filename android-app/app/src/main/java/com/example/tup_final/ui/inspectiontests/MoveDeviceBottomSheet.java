package com.example.tup_final.ui.inspectiontests;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet para mover un dispositivo a otra zona dentro de la misma location.
 */
public class MoveDeviceBottomSheet {

    private final BottomSheetDialog dialog;
    private final DeviceUiModel device;
    private final List<ZoneUiModel> availableZones;
    private final OnConfirmListener listener;

    private ZoneUiModel selectedZone;
    private RecyclerView recyclerZones;
    private MaterialButton btnConfirm;
    private MaterialButton btnCancel;
    private ProgressBar progressBar;
    private ZoneSelectAdapter adapter;

    public interface OnConfirmListener {
        void onConfirm(String targetZoneId);
    }

    public MoveDeviceBottomSheet(Context context, DeviceUiModel device,
                                 List<ZoneUiModel> availableZones, OnConfirmListener listener) {
        this.device = device;
        this.availableZones = availableZones != null ? availableZones : new ArrayList<>();
        this.listener = listener;
        this.dialog = new BottomSheetDialog(context);

        View root = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_move_device, null);
        dialog.setContentView(root);

        TextView subtitle = root.findViewById(R.id.text_move_device_subtitle);
        subtitle.setText(context.getString(R.string.move_device_subtitle, device.name));

        recyclerZones = root.findViewById(R.id.recycler_zones);
        btnConfirm = root.findViewById(R.id.btn_confirm_move);
        btnCancel = root.findViewById(R.id.btn_cancel_move);
        progressBar = root.findViewById(R.id.progress_move_device);

        List<ZoneUiModel> zonesExcludingCurrent = new ArrayList<>();
        for (ZoneUiModel z : this.availableZones) {
            if (!z.id.equals(device.zoneId)) {
                zonesExcludingCurrent.add(z);
            }
        }

        adapter = new ZoneSelectAdapter(zonesExcludingCurrent, zone -> {
            selectedZone = zone;
            btnConfirm.setEnabled(true);
            adapter.setSelectedZoneId(zone.id);
        });
        recyclerZones.setLayoutManager(new LinearLayoutManager(context));
        recyclerZones.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            if (selectedZone != null && listener != null) {
                listener.onConfirm(selectedZone.id);
            }
        });
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setLoading(boolean loading) {
        btnConfirm.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
        recyclerZones.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    public void showError(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(dialog.getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public DeviceUiModel getDevice() {
        return device;
    }

    private static class ZoneSelectAdapter extends RecyclerView.Adapter<ZoneSelectAdapter.ViewHolder> {
        private final List<ZoneUiModel> zones;
        private final OnZoneSelectListener listener;
        private String selectedZoneId;

        interface OnZoneSelectListener {
            void onZoneSelect(ZoneUiModel zone);
        }

        ZoneSelectAdapter(List<ZoneUiModel> zones, OnZoneSelectListener listener) {
            this.zones = zones;
            this.listener = listener;
        }

        void setSelectedZoneId(String zoneId) {
            String prev = selectedZoneId;
            selectedZoneId = zoneId;
            for (int i = 0; i < zones.size(); i++) {
                if (zones.get(i).id.equals(prev) || zones.get(i).id.equals(zoneId)) {
                    notifyItemChanged(i);
                }
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_zone_select, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ZoneUiModel zone = zones.get(position);
            holder.textZoneName.setText(zone.name);
            holder.itemView.setSelected(zone.id.equals(selectedZoneId));
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onZoneSelect(zone);
            });
        }

        @Override
        public int getItemCount() {
            return zones.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textZoneName;

            ViewHolder(View itemView) {
                super(itemView);
                textZoneName = itemView.findViewById(R.id.text_zone_name);
            }
        }
    }
}
