package com.example.tup_final.ui.usermanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.R;
import com.example.tup_final.data.remote.dto.UserProfileResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

/**
 * Adapter para la lista de usuarios en la pantalla de gestión (admin).
 * Muestra nombre, email y dropdown de rol (INSPECTOR/SUPERVISOR).
 * Los usuarios ADMIN muestran el rol como texto readonly.
 */
public class UserListAdapter extends ListAdapter<UserProfileResponse, UserListAdapter.ViewHolder> {

    public interface OnRoleChangeListener {
        void onRoleChange(UserProfileResponse user, String newRole);
    }

    private static final String ROLE_INSPECTOR = "INSPECTOR";
    private static final String ROLE_SUPERVISOR = "SUPERVISOR";
    private static final String ROLE_ADMIN = "ADMIN";

    private static final String[] EDITABLE_ROLES = {ROLE_INSPECTOR, ROLE_SUPERVISOR};

    private OnRoleChangeListener roleChangeListener;

    public void setOnRoleChangeListener(OnRoleChangeListener listener) {
        this.roleChangeListener = listener;
    }

    public UserListAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<UserProfileResponse> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<UserProfileResponse>() {
                @Override
                public boolean areItemsTheSame(@NonNull UserProfileResponse oldItem,
                                              @NonNull UserProfileResponse newItem) {
                    String oldId = oldItem != null && oldItem.getId() != null ? oldItem.getId() : "";
                    String newId = newItem != null && newItem.getId() != null ? newItem.getId() : "";
                    return oldId.equals(newId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull UserProfileResponse oldItem,
                                                  @NonNull UserProfileResponse newItem) {
                    return areItemsTheSame(oldItem, newItem)
                            && stringEquals(oldItem.getRole(), newItem.getRole())
                            && stringEquals(oldItem.getEmail(), newItem.getEmail())
                            && stringEquals(getFullName(oldItem), getFullName(newItem));
                }

                private boolean stringEquals(String a, String b) {
                    if (a == null) return b == null;
                    return a.equals(b);
                }

                private String getFullName(UserProfileResponse u) {
                    if (u == null) return "";
                    String first = u.getFirstName() != null ? u.getFirstName() : "";
                    String last = u.getLastName() != null ? u.getLastName() : "";
                    return (first + " " + last).trim();
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfileResponse user = getItem(position);
        holder.bind(user, roleChangeListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textUserName;
        private final TextView textUserEmail;
        private final MaterialAutoCompleteTextView inputRole;
        private final View layoutRole;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.text_user_name);
            textUserEmail = itemView.findViewById(R.id.text_user_email);
            inputRole = itemView.findViewById(R.id.input_role);
            layoutRole = itemView.findViewById(R.id.layout_role);
        }

        void bind(UserProfileResponse user, OnRoleChangeListener listener) {
            if (user == null) return;

            String fullName = getFullName(user);
            textUserName.setText(fullName.isEmpty() ? user.getEmail() : fullName);
            textUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");

            String role = user.getRole() != null ? user.getRole() : ROLE_INSPECTOR;

            if (ROLE_ADMIN.equalsIgnoreCase(role)) {
                layoutRole.setVisibility(View.VISIBLE);
                inputRole.setEnabled(false);
                inputRole.setText(itemView.getContext().getString(R.string.role_admin));
                inputRole.setOnItemClickListener(null);
            } else {
                layoutRole.setVisibility(View.VISIBLE);
                inputRole.setEnabled(true);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        itemView.getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        EDITABLE_ROLES
                );
                inputRole.setAdapter(adapter);
                inputRole.setText(role, false);

                inputRole.setOnItemClickListener((parent, view, pos, id) -> {
                    String selected = EDITABLE_ROLES[pos];
                    if (listener != null && !selected.equals(user.getRole())) {
                        listener.onRoleChange(user, selected);
                    }
                });
            }
        }

        private String getFullName(UserProfileResponse u) {
            if (u == null) return "";
            String first = u.getFirstName() != null ? u.getFirstName() : "";
            String last = u.getLastName() != null ? u.getLastName() : "";
            return (first + " " + last).trim();
        }
    }
}
