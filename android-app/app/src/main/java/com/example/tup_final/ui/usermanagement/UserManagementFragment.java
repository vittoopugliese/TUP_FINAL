package com.example.tup_final.ui.usermanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tup_final.R;
import com.example.tup_final.data.remote.dto.UserProfileResponse;
import com.example.tup_final.databinding.FragmentUserManagementBinding;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import static com.example.tup_final.util.Resource.Status.ERROR;
import static com.example.tup_final.util.Resource.Status.LOADING;
import static com.example.tup_final.util.Resource.Status.SUCCESS;

@AndroidEntryPoint
public class UserManagementFragment extends Fragment {

    private FragmentUserManagementBinding binding;
    private UserManagementViewModel viewModel;
    private UserListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserManagementViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        adapter = new UserListAdapter();
        adapter.setOnRoleChangeListener((user, newRole) ->
                viewModel.updateUserRole(user.getId(), newRole));

        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUsers.setAdapter(adapter);

        setupRoleDropdown();
        setupSearchField();

        observeAdminGuard();
        observeUsers();
        observeFilteredUsers();
        observeUpdateResult();
    }

    private void setupRoleDropdown() {
        String[] labels = new String[]{
                getString(R.string.filter_role_all),
                getString(R.string.role_inspector),
                getString(R.string.role_operator)
        };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                labels);
        binding.inputFilterRole.setAdapter(roleAdapter);
        binding.inputFilterRole.setText(labels[0], false);
        viewModel.setRoleFilterKey(UserManagementViewModel.FILTER_ROLE_ALL);
        binding.inputFilterRole.setOnItemClickListener((parent, view1, position, id) -> {
            String key = UserManagementViewModel.FILTER_ROLE_ALL;
            if (position == 1) {
                key = UserManagementViewModel.FILTER_ROLE_INSPECTOR;
            } else if (position == 2) {
                key = UserManagementViewModel.FILTER_ROLE_OPERATOR;
            }
            viewModel.setRoleFilterKey(key);
        });
    }

    private void setupSearchField() {
        binding.inputSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeAdminGuard() {
        viewModel.getIsAdminAllowed().observe(getViewLifecycleOwner(), allowed -> {
            if (allowed != null && !allowed) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void observeUsers() {
        viewModel.getUsersResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || binding == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressUsers.setVisibility(View.VISIBLE);
                    binding.textEmpty.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressUsers.setVisibility(View.GONE);
                    List<UserProfileResponse> list = resource.getData();
                    if (list == null || list.isEmpty()) {
                        binding.textEmpty.setVisibility(View.VISIBLE);
                        binding.textEmpty.setText(getString(R.string.user_management_empty));
                        adapter.submitList(new ArrayList<>());
                    } else {
                        binding.textEmpty.setVisibility(View.GONE);
                    }
                    break;
                case ERROR:
                    binding.progressUsers.setVisibility(View.GONE);
                    binding.textEmpty.setVisibility(View.VISIBLE);
                    binding.textEmpty.setText(resource.getMessage() != null
                            ? resource.getMessage() : getString(R.string.inspections_error));
                    adapter.submitList(new ArrayList<>());
                    break;
            }
        });
    }

    private void observeFilteredUsers() {
        viewModel.getFilteredUsers().observe(getViewLifecycleOwner(), filtered -> {
            if (binding == null) return;
            Resource<List<UserProfileResponse>> res = viewModel.getUsersResult().getValue();
            if (res == null || res.getStatus() == LOADING || res.getStatus() == ERROR) {
                return;
            }
            if (res.getStatus() != SUCCESS) {
                return;
            }

            adapter.submitList(filtered != null ? new ArrayList<>(filtered) : new ArrayList<>());

            int full = viewModel.getLastFetchedUserCount();
            boolean emptyFiltered = filtered == null || filtered.isEmpty();
            if (emptyFiltered && full > 0) {
                binding.textEmpty.setVisibility(View.VISIBLE);
                binding.textEmpty.setText(getString(R.string.user_management_no_results));
            } else if (emptyFiltered && full == 0) {
                binding.textEmpty.setVisibility(View.VISIBLE);
                binding.textEmpty.setText(getString(R.string.user_management_empty));
            } else {
                binding.textEmpty.setVisibility(View.GONE);
            }
        });
    }

    private void observeUpdateResult() {
        viewModel.getUpdateRoleResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.getStatus() == SUCCESS) {
                Toast.makeText(requireContext(),
                        getString(R.string.user_management_role_updated),
                        Toast.LENGTH_SHORT).show();
            } else if (resource.getStatus() == ERROR && resource.getMessage() != null) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
