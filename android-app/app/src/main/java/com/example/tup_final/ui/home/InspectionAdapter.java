package com.example.tup_final.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.example.tup_final.data.entity.InspectionEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para mostrar la lista de inspecciones en un RecyclerView.
 */
public class InspectionAdapter extends RecyclerView.Adapter<InspectionAdapter.ViewHolder> {

    private List<InspectionEntity> items = new ArrayList<>();

    public void setItems(List<InspectionEntity> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inspection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InspectionEntity item = items.get(position);
        holder.textType.setText(item.type != null && !item.type.isEmpty()
                ? item.type : holder.itemView.getContext().getString(R.string.inspection_type_unknown));
        holder.textStatus.setText(item.status != null && !item.status.isEmpty()
                ? item.status : holder.itemView.getContext().getString(R.string.inspection_status_unknown));
        holder.textDate.setText(item.scheduledDate != null && !item.scheduledDate.isEmpty()
                ? item.scheduledDate : holder.itemView.getContext().getString(R.string.inspection_date_unknown));
        holder.textBuilding.setText(item.buildingId != null && !item.buildingId.isEmpty()
                ? item.buildingId : holder.itemView.getContext().getString(R.string.inspection_building_unknown));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textType;
        final TextView textStatus;
        final TextView textDate;
        final TextView textBuilding;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textType = itemView.findViewById(R.id.text_inspection_type);
            textStatus = itemView.findViewById(R.id.text_inspection_status);
            textDate = itemView.findViewById(R.id.text_inspection_date);
            textBuilding = itemView.findViewById(R.id.text_inspection_building);
        }
    }
}
