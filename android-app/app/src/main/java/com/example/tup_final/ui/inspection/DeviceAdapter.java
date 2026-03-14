package com.example.tup_final.ui.inspection;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.example.tup_final.data.entity.DeviceEntity;
import com.example.tup_final.databinding.ItemDeviceBinding;

public class DeviceAdapter extends ListAdapter<DeviceEntity, DeviceAdapter.DeviceViewHolder> {

    private static final DiffUtil.ItemCallback<DeviceEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DeviceEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull DeviceEntity oldItem,
                                               @NonNull DeviceEntity newItem) {
                    return oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull DeviceEntity oldItem,
                                                  @NonNull DeviceEntity newItem) {
                    return oldItem.id.equals(newItem.id)
                            && (oldItem.name != null ? oldItem.name.equals(newItem.name) : newItem.name == null)
                            && oldItem.enabled == newItem.enabled;
                }
            };

    public DeviceAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeviceBinding binding = ItemDeviceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final ItemDeviceBinding binding;

        DeviceViewHolder(@NonNull ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DeviceEntity device) {
            binding.textDeviceName.setText(
                    device.name != null ? device.name : "Dispositivo sin nombre");

            binding.textDeviceCategory.setText(
                    device.deviceCategory != null
                            ? device.deviceCategory.replace("_", " ")
                            : "—");

            if (device.deviceSerialNumber != null) {
                binding.textSerialNumber.setText(
                        itemView.getContext().getString(R.string.device_serial_format,
                                device.deviceSerialNumber));
            } else {
                binding.textSerialNumber.setText(R.string.device_no_serial);
            }

            binding.viewStatusIndicator.setBackgroundColor(
                    itemView.getContext().getColor(
                            device.enabled
                                    ? android.R.color.holo_green_dark
                                    : android.R.color.holo_red_light));
        }
    }
}
