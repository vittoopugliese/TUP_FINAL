package com.example.tup_final.ui.locations;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.example.tup_final.data.entity.LocationWithStats;

/**
 * Adapter para la lista de ubicaciones.
 * Muestra: nombre, descripción, cantidad de tests e indicador de completitud (verde/amarillo).
 */
public class LocationAdapter extends ListAdapter<LocationWithStats, LocationAdapter.ViewHolder> {

    public interface OnLocationClickListener {
        void onLocationClick(LocationWithStats item);
    }

    private OnLocationClickListener clickListener;

    public void setOnLocationClickListener(OnLocationClickListener listener) {
        this.clickListener = listener;
    }

    private static final int COLOR_COMPLETE = 0xFF4CAF50;
    private static final int COLOR_INCOMPLETE = 0xFFFFC107;

    private static final DiffUtil.ItemCallback<LocationWithStats> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<LocationWithStats>() {
                @Override
                public boolean areItemsTheSame(@NonNull LocationWithStats oldItem,
                                              @NonNull LocationWithStats newItem) {
                    return oldItem.location.id.equals(newItem.location.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull LocationWithStats oldItem,
                                                 @NonNull LocationWithStats newItem) {
                    return oldItem.location.id.equals(newItem.location.id)
                            && oldItem.testCount == newItem.testCount
                            && oldItem.completedTestCount == newItem.completedTestCount
                            && stringEquals(oldItem.location.name, newItem.location.name)
                            && stringEquals(oldItem.location.details, newItem.location.details);
                }

                private boolean stringEquals(String a, String b) {
                    if (a == null) return b == null;
                    return a.equals(b);
                }
            };

    public LocationAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationWithStats item = getItem(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onLocationClick(item);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final View completenessIndicator;
        private final TextView textName;
        private final TextView textDescription;
        private final TextView textTests;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            completenessIndicator = itemView.findViewById(R.id.view_completeness_indicator);
            textName = itemView.findViewById(R.id.text_location_name);
            textDescription = itemView.findViewById(R.id.text_location_description);
            textTests = itemView.findViewById(R.id.text_location_tests);
        }

        void bind(LocationWithStats item) {
            textName.setText(item.location.name != null ? item.location.name : "-");
            textDescription.setText(item.location.details != null ? item.location.details : "-");
            textTests.setText(itemView.getContext().getString(
                    R.string.location_tests_progress, item.completedTestCount, item.testCount));

            int color = item.isComplete() ? COLOR_COMPLETE : COLOR_INCOMPLETE;
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setSize(36, 36);
            completenessIndicator.setBackground(drawable);
        }
    }
}
