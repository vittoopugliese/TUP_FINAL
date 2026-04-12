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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de gestión de usuarios (admin).
 * Lista usuarios y permite cambiar roles entre INSPECTOR y OPERATOR.
 * Verifica que el usuario actual sea ADMIN antes de mostrar contenido.
 */
@HiltViewModel
public class UserManagementViewModel extends ViewModel {

    /** Filtro de rol: mostrar todos los usuarios. */
    public static final String FILTER_ROLE_ALL = "ALL";
    /** Filtro de rol: solo INSPECTOR. */
    public static final String FILTER_ROLE_INSPECTOR = "INSPECTOR";
    /** Filtro de rol: solo OPERATOR. */
    public static final String FILTER_ROLE_OPERATOR = "OPERATOR";

    private final UserRepository userRepository;
    private final SharedPreferences prefs;

    private final MutableLiveData<Boolean> isAdminAllowed = new MutableLiveData<>(null);
    private final MediatorLiveData<Resource<UserEntity>> adminCheckMediator = new MediatorLiveData<>();
    private final Observer<Resource<UserEntity>> adminCheckObserver = r -> {};
    private final MutableLiveData<List<UserProfileResponse>> users = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<Resource<List<UserProfileResponse>>> usersResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<UserProfileResponse>> updateRoleResult = new MediatorLiveData<>();

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> roleFilterKey = new MutableLiveData<>(FILTER_ROLE_ALL);
    private final MediatorLiveData<List<UserProfileResponse>> filteredUsers = new MediatorLiveData<>();

    private int lastFetchedUserCount = 0;

    @Inject
    public UserManagementViewModel(UserRepository userRepository, SharedPreferences prefs) {
        this.userRepository = userRepository;
        this.prefs = prefs;
        initFilteredUsers();
        checkAdminAndLoad();
    }

    private void initFilteredUsers() {
        filteredUsers.addSource(users, list -> recomputeFiltered());
        filteredUsers.addSource(searchQuery, q -> recomputeFiltered());
        filteredUsers.addSource(roleFilterKey, k -> recomputeFiltered());
    }

    private void recomputeFiltered() {
        filteredUsers.setValue(applyFilters(
                users.getValue(),
                searchQuery.getValue(),
                roleFilterKey.getValue()));
    }

    private List<UserProfileResponse> applyFilters(List<UserProfileResponse> full,
                                                   String query,
                                                   String roleKey) {
        List<UserProfileResponse> base = full != null ? full : Collections.emptyList();
        if (base.isEmpty()) {
            return new ArrayList<>();
        }
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String key = roleKey == null ? FILTER_ROLE_ALL : roleKey;

        List<UserProfileResponse> out = new ArrayList<>();
        for (UserProfileResponse u : base) {
            if (!matchesRoleFilter(u, key)) {
                continue;
            }
            if (!matchesSearch(u, q)) {
                continue;
            }
            out.add(u);
        }
        return out;
    }

    private boolean matchesRoleFilter(UserProfileResponse u, String key) {
        if (FILTER_ROLE_ALL.equals(key)) {
            return true;
        }
        String r = u.getRole() != null ? u.getRole() : "";
        if (FILTER_ROLE_INSPECTOR.equals(key)) {
            return "INSPECTOR".equalsIgnoreCase(r);
        }
        if (FILTER_ROLE_OPERATOR.equals(key)) {
            return "OPERATOR".equalsIgnoreCase(r);
        }
        return true;
    }

    private boolean matchesSearch(UserProfileResponse u, String qLower) {
        if (qLower.isEmpty()) {
            return true;
        }
        String fn = safeLower(u.getFirstName());
        String ln = safeLower(u.getLastName());
        String em = safeLower(u.getEmail());
        String fullName = (fn + " " + ln).trim();
        return fn.contains(qLower) || ln.contains(qLower) || em.contains(qLower)
                || fullName.contains(qLower);
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
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
                lastFetchedUserCount = resource.getData().size();
                users.setValue(resource.getData());
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                lastFetchedUserCount = 0;
                users.setValue(new ArrayList<>());
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                lastFetchedUserCount = 0;
                users.setValue(new ArrayList<>());
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

    public void setSearchQuery(String query) {
        searchQuery.setValue(query != null ? query : "");
    }

    public void setRoleFilterKey(String key) {
        if (key == null) {
            roleFilterKey.setValue(FILTER_ROLE_ALL);
        } else {
            roleFilterKey.setValue(key);
        }
    }

    public int getLastFetchedUserCount() {
        return lastFetchedUserCount;
    }

    public LiveData<Resource<List<UserProfileResponse>>> getUsersResult() {
        return usersResult;
    }

    public LiveData<List<UserProfileResponse>> getUsers() {
        return users;
    }

    public LiveData<List<UserProfileResponse>> getFilteredUsers() {
        return filteredUsers;
    }

    public LiveData<Resource<UserProfileResponse>> getUpdateRoleResult() {
        return updateRoleResult;
    }
}
