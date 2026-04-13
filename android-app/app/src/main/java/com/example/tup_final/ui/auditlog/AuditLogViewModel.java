package com.example.tup_final.ui.auditlog;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.remote.dto.AuditLogResponse;
import com.example.tup_final.data.repository.AuditLogRepository;
import com.example.tup_final.data.repository.UserRepository;
import com.example.tup_final.util.Resource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Pantalla de auditoría (solo ADMIN). Filtros por acción y rango de fechas.
 */
@HiltViewModel
public class AuditLogViewModel extends ViewModel {

    /** Sin filtro de acción (envía null al API). */
    public static final String FILTER_ACTION_ALL = "ALL";
    public static final String ACTION_ROLE_CHANGE = "ROLE_CHANGE";
    public static final String ACTION_ASSIGNMENT_ADD = "ASSIGNMENT_ADD";
    public static final String ACTION_ASSIGNMENT_REMOVE = "ASSIGNMENT_REMOVE";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_SIGN = "SIGN";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final SharedPreferences prefs;

    private final MutableLiveData<Boolean> isAdminAllowed = new MutableLiveData<>(null);
    private final MediatorLiveData<Resource<UserEntity>> adminCheckMediator = new MediatorLiveData<>();
    private final Observer<Resource<UserEntity>> adminCheckObserver = r -> {};

    private final MutableLiveData<String> actionFilterKey = new MutableLiveData<>(FILTER_ACTION_ALL);
    private final MutableLiveData<Long> dateFromMillis = new MutableLiveData<>(null);
    private final MutableLiveData<Long> dateToMillis = new MutableLiveData<>(null);

    private final MediatorLiveData<Resource<List<AuditLogResponse>>> logsResult = new MediatorLiveData<>();

    @Inject
    public AuditLogViewModel(AuditLogRepository auditLogRepository,
                             UserRepository userRepository,
                             SharedPreferences prefs) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.prefs = prefs;
        checkAdminAndLoad();
    }

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
                    loadLogs();
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

    public void setActionFilterKey(String key) {
        actionFilterKey.setValue(key != null ? key : FILTER_ACTION_ALL);
    }

    public void setDateRangeMillis(Long from, Long to) {
        dateFromMillis.setValue(from);
        dateToMillis.setValue(to);
    }

    public void clearDateRange() {
        dateFromMillis.setValue(null);
        dateToMillis.setValue(null);
    }

    /**
     * Recarga con los filtros actuales.
     */
    public void loadLogs() {
        String actionParam = null;
        String key = actionFilterKey.getValue();
        if (key != null && !FILTER_ACTION_ALL.equals(key)) {
            actionParam = key;
        }

        String fromIso = null;
        String toIso = null;
        Long fromM = dateFromMillis.getValue();
        Long toM = dateToMillis.getValue();
        if (fromM != null && toM != null) {
            LocalDate fromD = Instant.ofEpochMilli(fromM).atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate toD = Instant.ofEpochMilli(toM).atZone(ZoneOffset.UTC).toLocalDate();
            Instant fromInst = fromD.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant toInst = toD.atTime(23, 59, 59, 999_000_000).atZone(ZoneOffset.UTC).toInstant();
            fromIso = DateTimeFormatter.ISO_INSTANT.format(fromInst);
            toIso = DateTimeFormatter.ISO_INSTANT.format(toInst);
        }

        LiveData<Resource<List<AuditLogResponse>>> source =
                auditLogRepository.getAuditLogs(actionParam, fromIso, toIso);
        logsResult.addSource(source, resource -> {
            logsResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                logsResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<List<AuditLogResponse>>> getLogsResult() {
        return logsResult;
    }
}
