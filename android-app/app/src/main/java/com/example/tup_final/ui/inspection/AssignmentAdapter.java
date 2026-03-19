package com.example.tup_final.ui.inspection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tup_final.data.entity.InspectionAssignmentEntity;
import com.example.tup_final.databinding.ItemAssignmentBinding;

import java.util.function.Predicate;

public class AssignmentAdapter extends ListAdapter<InspectionAssignmentEntity, AssignmentAdapter.ViewHolder> {

    public interface OnRemoveClickListener {
        void onRemove(InspectionAssignmentEntity assignment);
    }

    private final OnRemoveClickListener onRemoveClickListener;
    private final Predicate<InspectionAssignmentEntity> canRemovePredicate;

    private static final DiffUtil.ItemCallback<InspectionAssignmentEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<InspectionAssignmentEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull InspectionAssignmentEntity oldItem,
                                               @NonNull InspectionAssignmentEntity newItem) {
                    return oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull InspectionAssignmentEntity oldItem,
                                                  @NonNull InspectionAssignmentEntity newItem) {
                    return oldItem.id.equals(newItem.id)
                            && (oldItem.userEmail != null ? oldItem.userEmail.equals(newItem.userEmail) : newItem.userEmail == null)
                            && (oldItem.role != null ? oldItem.role.equals(newItem.role) : newItem.role == null);
                }
            };

    public AssignmentAdapter(OnRemoveClickListener onRemoveClickListener,
                             Predicate<InspectionAssignmentEntity> canRemovePredicate) {
        super(DIFF_CALLBACK);
        this.onRemoveClickListener = onRemoveClickListener;
        this.canRemovePredicate = canRemovePredicate != null ? canRemovePredicate : a -> true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAssignmentBinding binding = ItemAssignmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAssignmentBinding binding;

        ViewHolder(@NonNull ItemAssignmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(InspectionAssignmentEntity assignment) {
            binding.textEmail.setText(assignment.userEmail != null ? assignment.userEmail : "");
            boolean canRemove = canRemovePredicate.test(assignment);
            binding.btnRemove.setVisibility(canRemove ? View.VISIBLE : View.GONE);
            binding.btnRemove.setOnClickListener(v -> {
                if (canRemove && onRemoveClickListener != null) {
                    onRemoveClickListener.onRemove(assignment);
                }
            });
        }
    }
}
