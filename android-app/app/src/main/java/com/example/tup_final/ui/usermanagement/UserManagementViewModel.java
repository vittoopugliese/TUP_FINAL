package com.example.tup_final.ui.usermanagement;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.remote.dto.UserProfileResponse;
import com.example.tup_final.data.repository.UserRepository;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de gestión de usuarios (admin).
 * Lista usuarios y permite cambiar roles entre INSPECTOR y OPERATOR.
 * Verifica que el usuario actual sea ADMIN antes de mostrar contenido.
 */
@HiltViewModel
public class UserManagementViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final SharedPreferences prefs;

    private final MutableLiveData<Boolean> isAdminAllowed = new MutableLiveData<>(null);
    private final MediatorLiveData<Resource<UserEntity>> adminCheckMediator = new MediatorLiveData<>();
    private final Observer<Resource<UserEntity>> adminCheckObserver = r -> {};
    private final MutableLiveData<List<UserProfileResponse>> users = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<Resource<List<UserProfileResponse>>> usersResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<UserProfileResponse>> updateRoleResult = new MediatorLiveData<>();

    @Inject
    public UserManagementViewModel(UserRepository userRepository, SharedPreferences prefs) {
        this.userRepository = userRepository;
        this.prefs = prefs;
        checkAdminAndLoad();
    }

    /**
     * Verifica que el usuario actual sea ADMIN. Si lo es, carga usuarios. Si no, emite false.
     */
    private void checkAdminAndLoad() {
        String userId = prefs.getString("cached_user_id", null);
        if (userId == null || userId.isEmpty()) {
            isAdminAllowed.setValue(false);
            return;
        }
        LiveData<Resource<UserEntity>> source = userRepository.getUserProfile(userId);
        adminCheckMediator.addSource(source, resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                boolean admin = "ADMIN".equalsIgnoreCase(resource.getData().role);
                isAdminAllowed.setValue(admin);
                if (admin) {
                    loadUsers();
                }
            } else if (resource != null && resource.getStatus() == Resource.Status.ERROR) {
                isAdminAllowed.setValue(false);
            }
            if (resource != null && resource.getStatus() != Resource.Status.LOADING) {
                adminCheckMediator.removeSource(source);
            }
        });
        adminCheckMediator.observeForever(adminCheckObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        adminCheckMediator.removeObserver(adminCheckObserver);
    }

    public LiveData<Boolean> getIsAdminAllowed() {
        return isAdminAllowed;
    }

    /**
     * Carga la lista de usuarios desde el backend.
     */
    public void loadUsers() {
        LiveData<Resource<List<UserProfileResponse>>> source = userRepository.getAllUsers();
        usersResult.addSource(source, resource -> {
            usersResult.setValue(resource);
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                users.setValue(resource.getData());
            }
            if (resource.getStatus() != Resource.Status.LOADING) {
                usersResult.removeSource(source);
            }
        });
    }

    /**
     * Actualiza el rol de un usuario.
     *
     * @param userId  ID del usuario
     * @param newRole INSPECTOR u OPERATOR
     */
    public void updateUserRole(String userId, String newRole) {
        LiveData<Resource<UserProfileResponse>> source = userRepository.updateUserRole(userId, newRole);
        updateRoleResult.addSource(source, resource -> {
            updateRoleResult.setValue(resource);
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                loadUsers();
            }
            if (resource.getStatus() != Resource.Status.LOADING) {
                updateRoleResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<List<UserProfileResponse>>> getUsersResult() {
        return usersResult;
    }

    public LiveData<List<UserProfileResponse>> getUsers() {
        return users;
    }

    public LiveData<Resource<UserProfileResponse>> getUpdateRoleResult() {
        return updateRoleResult;
    }
}
