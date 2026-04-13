package com.example.tup_final.ui.auditlog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.example.tup_final.data.remote.dto.AuditLogResponse;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Lista de entradas de auditoría.
 */
public class AuditLogAdapter extends ListAdapter<AuditLogResponse, AuditLogAdapter.ViewHolder> {

    private static final DateTimeFormatter WHEN_FORMAT =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault());

    public AuditLogAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<AuditLogResponse> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AuditLogResponse>() {
                @Override
                public boolean areItemsTheSame(@NonNull AuditLogResponse oldItem,
                                               @NonNull AuditLogResponse newItem) {
                    String o = oldItem.getId() != null ? oldItem.getId() : "";
                    String n = newItem.getId() != null ? newItem.getId() : "";
                    return o.equals(n);
                }

                @Override
                public boolean areContentsTheSame(@NonNull AuditLogResponse oldItem,
                                                  @NonNull AuditLogResponse newItem) {
                    return stringEq(oldItem.getAction(), newItem.getAction())
                            && stringEq(oldItem.getEntityType(), newItem.getEntityType())
                            && stringEq(oldItem.getEntityId(), newItem.getEntityId())
                            && stringEq(oldItem.getUserId(), newItem.getUserId())
                            && stringEq(oldItem.getCreatedAt(), newItem.getCreatedAt())
                            && stringEq(oldItem.getMetadataJson(), newItem.getMetadataJson());
                }

                private boolean stringEq(String a, String b) {
                    if (a == null) return b == null;
                    return a.equals(b);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audit_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textAction;
        private final TextView textEntity;
        private final TextView textActor;
        private final TextView textWhen;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textAction = itemView.findViewById(R.id.text_action);
            textEntity = itemView.findViewById(R.id.text_entity);
            textActor = itemView.findViewById(R.id.text_actor);
            textWhen = itemView.findViewById(R.id.text_when);
        }

        void bind(AuditLogResponse row) {
            if (row == null) return;
            textAction.setText(row.getAction() != null ? row.getAction() : "");
            String entity = (row.getEntityType() != null ? row.getEntityType() : "")
                    + " · "
                    + (row.getEntityId() != null ? row.getEntityId() : "");
            textEntity.setText(entity.trim());
            textActor.setText(row.getUserId() != null ? row.getUserId() : "");
            textWhen.setText(formatWhen(row.getCreatedAt()));
        }

        private String formatWhen(String createdAtIso) {
            if (createdAtIso == null || createdAtIso.isEmpty()) {
                return "";
            }
            try {
                Instant instant = Instant.parse(createdAtIso);
                return WHEN_FORMAT.format(instant);
            } catch (Exception e) {
                return createdAtIso;
            }
        }
    }
}
