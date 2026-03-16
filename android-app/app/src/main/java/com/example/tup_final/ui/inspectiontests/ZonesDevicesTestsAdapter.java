package com.example.tup_final.ui.inspectiontests;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adapter para la lista jerárquica Zona -> Device -> Test.
 * Usa una lista plana de ítems con tipos distintos.
 */
public class ZonesDevicesTestsAdapter extends ListAdapter<ZonesDevicesTestsAdapter.ListItem, RecyclerView.ViewHolder> {

    private static final int TYPE_ZONE = 0;
    private static final int TYPE_DEVICE = 1;
    private static final int TYPE_TEST = 2;

    /** Estados normalizados para tests. Fallback: PENDING. */
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    /**
     * Normaliza el estado del test para UI. Maneja null, variantes (DONE_FAILED, etc.) y minúsculas.
     */
    private static String normalizeTestStatus(String status) {
        if (status == null || status.isEmpty()) return STATUS_PENDING;
        String s = status.trim().toUpperCase();
        if ("COMPLETED".equals(s)) return STATUS_COMPLETED;
        if ("FAILED".equals(s) || "DONE_FAILED".equals(s)) return STATUS_FAILED;
        return STATUS_PENDING;
    }

    public interface OnTestClickListener {
        void onTestClick(TestUiModel test);
    }

    public interface OnZoneExpandListener {
        void onZoneExpand(String zoneId);
    }

    public interface OnDeviceExpandListener {
        void onDeviceExpand(String deviceId);
    }

    public interface OnAddDeviceListener {
        void onAddDevice(ZoneUiModel zone);
    }

    public interface OnMoveDeviceListener {
        void onMoveDevice(DeviceUiModel device);
    }

    private OnTestClickListener testClickListener;
    private OnZoneExpandListener zoneExpandListener;
    private OnDeviceExpandListener deviceExpandListener;
    private OnAddDeviceListener addDeviceListener;
    private OnMoveDeviceListener moveDeviceListener;
    private Set<String> expandedZoneIds;
    private Set<String> expandedDeviceIds;

    public void setOnTestClickListener(OnTestClickListener listener) {
        this.testClickListener = listener;
    }

    public void setOnZoneExpandListener(OnZoneExpandListener listener) {
        this.zoneExpandListener = listener;
    }

    public void setOnDeviceExpandListener(OnDeviceExpandListener listener) {
        this.deviceExpandListener = listener;
    }

    public void setOnAddDeviceListener(OnAddDeviceListener listener) {
        this.addDeviceListener = listener;
    }

    public void setOnMoveDeviceListener(OnMoveDeviceListener listener) {
        this.moveDeviceListener = listener;
    }

    public void setExpandedState(Set<String> expandedZoneIds, Set<String> expandedDeviceIds) {
        this.expandedZoneIds = expandedZoneIds;
        this.expandedDeviceIds = expandedDeviceIds;
    }

    public ZonesDevicesTestsAdapter() {
        super(new DiffUtil.ItemCallback<ListItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                if (oldItem.getType() != newItem.getType()) return false;
                switch (oldItem.getType()) {
                    case TYPE_ZONE:
                        return ((ZoneItem) oldItem).zone.id.equals(((ZoneItem) newItem).zone.id);
                    case TYPE_DEVICE:
                        return ((DeviceItem) oldItem).device.id.equals(((DeviceItem) newItem).device.id);
                    case TYPE_TEST:
                        return ((TestItem) oldItem).test.id.equals(((TestItem) newItem).test.id);
                    default:
                        return false;
                }
            }

            @Override
            public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                if (oldItem.getType() != newItem.getType()) return false;
                if (oldItem.getType() == TYPE_TEST) {
                    TestItem o = (TestItem) oldItem;
                    TestItem n = (TestItem) newItem;
                    return o.test.id.equals(n.test.id)
                            && java.util.Objects.equals(o.test.status, n.test.status);
                }
                return areItemsTheSame(oldItem, newItem);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ZONE) {
            return new ZoneViewHolder(inflater.inflate(R.layout.item_zone_header, parent, false));
        }
        if (viewType == TYPE_DEVICE) {
            return new DeviceViewHolder(inflater.inflate(R.layout.item_device_header, parent, false));
        }
        return new TestViewHolder(inflater.inflate(R.layout.item_test_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = getItem(position);
        if (item.getType() == TYPE_ZONE) {
            ((ZoneViewHolder) holder).bind((ZoneItem) item);
        } else if (item.getType() == TYPE_DEVICE) {
            ((DeviceViewHolder) holder).bind((DeviceItem) item);
        } else {
            ((TestViewHolder) holder).bind((TestItem) item);
        }
    }

    abstract static class ListItem {
        abstract int getType();
    }

    static class ZoneItem extends ListItem {
        final ZoneUiModel zone;
        final boolean expanded;

        ZoneItem(ZoneUiModel zone, boolean expanded) {
            this.zone = zone;
            this.expanded = expanded;
        }

        @Override
        int getType() { return TYPE_ZONE; }
    }

    static class DeviceItem extends ListItem {
        final DeviceUiModel device;
        final boolean expanded;

        DeviceItem(DeviceUiModel device, boolean expanded) {
            this.device = device;
            this.expanded = expanded;
        }

        @Override
        int getType() { return TYPE_DEVICE; }
    }

    static class TestItem extends ListItem {
        final TestUiModel test;

        TestItem(TestUiModel test) {
            this.test = test;
        }

        @Override
        int getType() { return TYPE_TEST; }
    }

    static List<ListItem> buildFlatList(List<ZoneUiModel> zones,
                                        Set<String> expandedZoneIds,
                                        Set<String> expandedDeviceIds) {
        List<ListItem> items = new ArrayList<>();
        if (zones == null) return items;

        for (ZoneUiModel z : zones) {
            boolean zoneExpanded = expandedZoneIds != null && expandedZoneIds.contains(z.id);
            items.add(new ZoneItem(z, zoneExpanded));

            if (zoneExpanded && z.devices != null) {
                for (DeviceUiModel d : z.devices) {
                    boolean deviceExpanded = expandedDeviceIds != null && expandedDeviceIds.contains(d.id);
                    items.add(new DeviceItem(d, deviceExpanded));

                    if (deviceExpanded && d.tests != null) {
                        for (TestUiModel t : d.tests) {
                            items.add(new TestItem(t));
                        }
                    }
                }
            }
        }
        return items;
    }

    class ZoneViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconExpand;
        private final TextView textZoneName;
        private final TextView textDevicesCount;
        private final View btnAddDevice;

        ZoneViewHolder(View itemView) {
            super(itemView);
            iconExpand = itemView.findViewById(R.id.icon_zone_expand);
            textZoneName = itemView.findViewById(R.id.text_zone_name);
            textDevicesCount = itemView.findViewById(R.id.text_zone_devices_count);
            btnAddDevice = itemView.findViewById(R.id.btn_add_device);

            itemView.setOnClickListener(v -> {
                if (v == btnAddDevice) return;
                int pos = getAdapterPosition();
                if (pos >= 0 && zoneExpandListener != null) {
                    ZoneItem item = (ZoneItem) getItem(pos);
                    zoneExpandListener.onZoneExpand(item.zone.id);
                }
            });

            btnAddDevice.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos >= 0 && addDeviceListener != null) {
                    ZoneItem item = (ZoneItem) getItem(pos);
                    addDeviceListener.onAddDevice(item.zone);
                }
            });
        }

        void bind(ZoneItem item) {
            textZoneName.setText(item.zone.name);
            int count = item.zone.devices != null ? item.zone.devices.size() : 0;
            textDevicesCount.setText(itemView.getContext().getString(R.string.zone_devices_count, count));

            float rotation = item.expanded ? 180f : 0f;
            iconExpand.setRotation(rotation);
        }
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final View viewStatus;
        private final ImageView iconExpand;
        private final TextView textDeviceName;
        private final TextView textTestsSummary;

        DeviceViewHolder(View itemView) {
            super(itemView);
            viewStatus = itemView.findViewById(R.id.view_device_status);
            iconExpand = itemView.findViewById(R.id.icon_device_expand);
            textDeviceName = itemView.findViewById(R.id.text_device_name);
            textTestsSummary = itemView.findViewById(R.id.text_device_tests_summary);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos >= 0 && deviceExpandListener != null) {
                    DeviceItem item = (DeviceItem) getItem(pos);
                    deviceExpandListener.onDeviceExpand(item.device.id);
                }
            });
            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos >= 0 && moveDeviceListener != null) {
                    DeviceItem item = (DeviceItem) getItem(pos);
                    moveDeviceListener.onMoveDevice(item.device);
                    return true;
                }
                return false;
            });
        }

        void bind(DeviceItem item) {
            textDeviceName.setText(item.device.name);
            int pending = 0, completed = 0, failed = 0;
            if (item.device.tests != null) {
                for (TestUiModel t : item.device.tests) {
                    String s = normalizeTestStatus(t.status);
                    if (STATUS_COMPLETED.equals(s)) completed++;
                    else if (STATUS_FAILED.equals(s)) failed++;
                    else pending++;
                }
            }
            textTestsSummary.setText(itemView.getContext().getString(
                    R.string.device_tests_summary, pending, completed, failed));

            int colorRes = item.device.enabled ? android.R.color.holo_green_dark : android.R.color.holo_red_light;
            viewStatus.setBackgroundColor(itemView.getContext().getColor(colorRes));

            float rotation = item.expanded ? 180f : 0f;
            iconExpand.setRotation(rotation);
        }
    }

    class TestViewHolder extends RecyclerView.ViewHolder {
        private final View viewStatus;
        private final TextView textTestName;
        private final TextView textTestStatusLabel;

        TestViewHolder(View itemView) {
            super(itemView);
            viewStatus = itemView.findViewById(R.id.view_test_status);
            textTestName = itemView.findViewById(R.id.text_test_name);
            textTestStatusLabel = itemView.findViewById(R.id.text_test_status_label);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos >= 0 && testClickListener != null) {
                    TestItem item = (TestItem) getItem(pos);
                    testClickListener.onTestClick(item.test);
                }
            });
        }

        void bind(TestItem item) {
            textTestName.setText(item.test.name);
            String normalized = normalizeTestStatus(item.test.status);

            int colorRes;
            int labelRes;
            if (STATUS_COMPLETED.equals(normalized)) {
                colorRes = android.R.color.holo_green_dark;
                labelRes = R.string.test_status_completed;
            } else if (STATUS_FAILED.equals(normalized)) {
                colorRes = android.R.color.holo_red_dark;
                labelRes = R.string.test_status_failed;
            } else {
                colorRes = android.R.color.holo_orange_light;
                labelRes = R.string.test_status_pending;
            }
            viewStatus.setBackgroundColor(itemView.getContext().getColor(colorRes));
            textTestStatusLabel.setText(labelRes);
        }
    }
}
