package com.example.tup_final.ui.home;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.example.tup_final.data.entity.InspectionEntity;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter para las tarjetas de inspección.
 * Muestra: edificio, fecha, estado (badge con color), días restantes.
 *
 * Colores por estado:
 * - PENDING → Gris (#9E9E9E)
 * - IN_PROGRESS → Amarillo (#FFC107)
 * - DONE_COMPLETED → Verde (#4CAF50)
 * - DONE_FAILED → Rojo (#F44336)
 */
public class InspectionAdapter extends ListAdapter<InspectionEntity, InspectionAdapter.ViewHolder> {

    // Colores por estado
    private static final int COLOR_PENDING = Color.parseColor("#9E9E9E");
    private static final int COLOR_IN_PROGRESS = Color.parseColor("#FFC107");
    private static final int COLOR_DONE_COMPLETED = Color.parseColor("#4CAF50");
    private static final int COLOR_DONE_FAILED = Color.parseColor("#F44336");

    private static final SimpleDateFormat ISO_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public InspectionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<InspectionEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<InspectionEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull InspectionEntity oldItem,
                                                @NonNull InspectionEntity newItem) {
                    return oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull InspectionEntity oldItem,
                                                   @NonNull InspectionEntity newItem) {
                    return oldItem.id.equals(newItem.id)
                            && stringEquals(oldItem.status, newItem.status)
                            && stringEquals(oldItem.buildingId, newItem.buildingId)
                            && stringEquals(oldItem.scheduledDate, newItem.scheduledDate);
                }

                private boolean stringEquals(String a, String b) {
                    if (a == null) return b == null;
                    return a.equals(b);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inspection_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InspectionEntity inspection = getItem(position);
        holder.bind(inspection);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final View statusIndicator;
        private final TextView textBuilding;
        private final TextView chipStatus;
        private final TextView textDate;
        private final TextView textDaysRemaining;
        private final TextView textType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_inspection);
            statusIndicator = itemView.findViewById(R.id.view_status_indicator);
            textBuilding = itemView.findViewById(R.id.text_building);
            chipStatus = itemView.findViewById(R.id.chip_status);
            textDate = itemView.findViewById(R.id.text_date);
            textDaysRemaining = itemView.findViewById(R.id.text_days_remaining);
            textType = itemView.findViewById(R.id.text_type);
        }

        void bind(InspectionEntity inspection) {
            // Color según estado
            int statusColor = getStatusColor(inspection.status);

            // Borde de la card
            card.setStrokeColor(statusColor);

            // Indicador vertical
            statusIndicator.setBackgroundColor(statusColor);

            // Edificio (usar buildingId como nombre si no hay tabla de edificios)
            String buildingName = inspection.buildingId != null
                    ? inspection.buildingId : itemView.getContext().getString(R.string.inspection_no_building);
            textBuilding.setText(buildingName);

            // Badge de estado
            chipStatus.setText(getStatusLabel(inspection.status));
            GradientDrawable badgeBg = (GradientDrawable) chipStatus.getBackground().mutate();
            badgeBg.setColor(statusColor);

            // Fecha
            String formattedDate = formatDate(inspection.scheduledDate);
            textDate.setText(formattedDate);

            // Días restantes
            String daysText = calculateDaysRemaining(inspection.scheduledDate, inspection.status);
            textDaysRemaining.setText(daysText);
            textDaysRemaining.setTextColor(statusColor);

            // Tipo
            String typeLabel = getTypeLabel(inspection.type);
            textType.setText(typeLabel);
        }

        private int getStatusColor(String status) {
            if (status == null) return COLOR_PENDING;
            switch (status) {
                case "IN_PROGRESS":
                    return COLOR_IN_PROGRESS;
                case "DONE_COMPLETED":
                    return COLOR_DONE_COMPLETED;
                case "DONE_FAILED":
                    return COLOR_DONE_FAILED;
                case "PENDING":
                default:
                    return COLOR_PENDING;
            }
        }

        private String getStatusLabel(String status) {
            if (status == null) return itemView.getContext().getString(R.string.status_pending);
            switch (status) {
                case "IN_PROGRESS":
                    return itemView.getContext().getString(R.string.status_in_progress);
                case "DONE_COMPLETED":
                    return itemView.getContext().getString(R.string.status_completed);
                case "DONE_FAILED":
                    return itemView.getContext().getString(R.string.status_failed);
                case "PENDING":
                default:
                    return itemView.getContext().getString(R.string.status_pending);
            }
        }

        private String getTypeLabel(String type) {
            if (type == null) return "";
            switch (type) {
                case "Daily":
                    return itemView.getContext().getString(R.string.type_daily);
                case "Weekly":
                    return itemView.getContext().getString(R.string.type_weekly);
                case "Monthly":
                    return itemView.getContext().getString(R.string.type_monthly);
                case "Annually":
                    return itemView.getContext().getString(R.string.type_annually);
                default:
                    return type;
            }
        }

        private String formatDate(String isoDate) {
            if (isoDate == null || isoDate.isEmpty()) return "—";
            try {
                // Intentar parsear ISO con zona horaria
                String cleanDate = isoDate;
                if (cleanDate.contains("Z")) {
                    cleanDate = cleanDate.replace("Z", "+0000");
                }
                // Intentar con el formato ISO estándar
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = parser.parse(cleanDate.substring(0, Math.min(cleanDate.length(), 19)));
                if (date != null) {
                    return DISPLAY_FORMAT.format(date);
                }
            } catch (ParseException e) {
                // Intentar formato simple
                try {
                    SimpleDateFormat simpleParser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date = simpleParser.parse(isoDate.substring(0, Math.min(isoDate.length(), 10)));
                    if (date != null) {
                        return DISPLAY_FORMAT.format(date);
                    }
                } catch (ParseException ignored) {}
            }
            return isoDate;
        }

        private String calculateDaysRemaining(String isoDate, String status) {
            // Si la inspección ya finalizó, no mostrar días restantes
            if ("DONE_COMPLETED".equals(status) || "DONE_FAILED".equals(status)) {
                return "DONE_COMPLETED".equals(status)
                        ? itemView.getContext().getString(R.string.inspection_completed)
                        : itemView.getContext().getString(R.string.inspection_failed);
            }

            if (isoDate == null || isoDate.isEmpty()) return "";

            try {
                String cleanDate = isoDate;
                if (cleanDate.contains("Z")) {
                    cleanDate = cleanDate.replace("Z", "+0000");
                }
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date scheduledDate = parser.parse(cleanDate.substring(0, Math.min(cleanDate.length(), 19)));

                if (scheduledDate != null) {
                    long diffMs = scheduledDate.getTime() - System.currentTimeMillis();
                    long days = TimeUnit.MILLISECONDS.toDays(diffMs);

                    if (days < 0) {
                        return Math.abs(days) + " " + itemView.getContext().getString(R.string.inspection_days_overdue);
                    } else if (days == 0) {
                        return itemView.getContext().getString(R.string.inspection_today);
                    } else if (days == 1) {
                        return "1 " + itemView.getContext().getString(R.string.inspection_day_remaining);
                    } else {
                        return days + " " + itemView.getContext().getString(R.string.inspection_days_remaining);
                    }
                }
            } catch (ParseException ignored) {}

            return "";
        }
    }
}
