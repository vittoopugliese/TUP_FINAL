package com.example.tup_final.ui.auditlog;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.remote.dto.AuditLogResponse;
import com.example.tup_final.data.remote.dto.UserProfileResponse;
import com.example.tup_final.data.repository.AuditLogRepository;
import com.example.tup_final.data.repository.InspectionRepository;
import com.example.tup_final.data.repository.UserRepository;
import com.example.tup_final.util.Resource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuditLogViewModel extends ViewModel {

    public static final String FILTER_ACTION_ALL = "ALL";
    public static final String ACTION_ROLE_CHANGE = "ROLE_CHANGE";
    public static final String ACTION_ASSIGNMENT_ADD = "ASSIGNMENT_ADD";
    public static final String ACTION_ASSIGNMENT_REMOVE = "ASSIGNMENT_REMOVE";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_SIGN = "SIGN";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final InspectionRepository inspectionRepository;
    private final SharedPreferences prefs;

    private final MutableLiveData<Boolean> isAdminAllowed = new MutableLiveData<>(null);
    private final MediatorLiveData<Resource<UserEntity>> adminCheckMediator = new MediatorLiveData<>();
    private final Observer<Resource<UserEntity>> adminCheckObserver = r -> {};

    private final MutableLiveData<String> actionFilterKey = new MutableLiveData<>(FILTER_ACTION_ALL);
    private final MutableLiveData<String> userEmailFilter = new MutableLiveData<>(null);
    private final MutableLiveData<String> entityIdFilter = new MutableLiveData<>(null);
    private final MutableLiveData<Long> dateFromMillis = new MutableLiveData<>(null);
    private final MutableLiveData<Long> dateToMillis = new MutableLiveData<>(null);

    private final MediatorLiveData<Resource<List<AuditLogResponse>>> logsResult = new MediatorLiveData<>();
    private final MutableLiveData<List<UserProfileResponse>> usersForDropdown = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<InspectionEntity>> inspectionsForDropdown = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<Resource<byte[]>> pdfResult = new MediatorLiveData<>();

    @Inject
    public AuditLogViewModel(AuditLogRepository auditLogRepository,
                             UserRepository userRepository,
                             InspectionRepository inspectionRepository,
                             SharedPreferences prefs) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.inspectionRepository = inspectionRepository;
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
                    loadUsers();
                    loadInspections();
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

    private void loadUsers() {
        LiveData<Resource<List<UserProfileResponse>>> src = userRepository.getAllUsers();
        MediatorLiveData<Resource<List<UserProfileResponse>>> mediator = new MediatorLiveData<>();
        mediator.addSource(src, resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                usersForDropdown.setValue(resource.getData());
            }
            if (resource != null && resource.getStatus() != Resource.Status.LOADING) {
                mediator.removeSource(src);
            }
        });
        mediator.observeForever(r -> {});
    }

    private void loadInspections() {
        LiveData<Resource<List<InspectionEntity>>> src = inspectionRepository.getInspections();
        MediatorLiveData<Resource<List<InspectionEntity>>> mediator = new MediatorLiveData<>();
        mediator.addSource(src, resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                inspectionsForDropdown.setValue(resource.getData());
            }
            if (resource != null && resource.getStatus() != Resource.Status.LOADING) {
                mediator.removeSource(src);
            }
        });
        mediator.observeForever(r -> {});
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        adminCheckMediator.removeObserver(adminCheckObserver);
    }

    public LiveData<Boolean> getIsAdminAllowed() { return isAdminAllowed; }
    public LiveData<List<UserProfileResponse>> getUsersForDropdown() { return usersForDropdown; }
    public LiveData<List<InspectionEntity>> getInspectionsForDropdown() { return inspectionsForDropdown; }
    public LiveData<Resource<List<AuditLogResponse>>> getLogsResult() { return logsResult; }
    public LiveData<Resource<byte[]>> getPdfResult() { return pdfResult; }

    public void setActionFilterKey(String key) {
        actionFilterKey.setValue(key != null ? key : FILTER_ACTION_ALL);
    }

    public void setUserFilter(String email) {
        userEmailFilter.setValue(email);
    }

    public void setEntityIdFilter(String inspectionId) {
        entityIdFilter.setValue(inspectionId);
    }

    public void setDateRangeMillis(Long from, Long to) {
        dateFromMillis.setValue(from);
        dateToMillis.setValue(to);
    }

    public void clearAllFilters() {
        actionFilterKey.setValue(FILTER_ACTION_ALL);
        userEmailFilter.setValue(null);
        entityIdFilter.setValue(null);
        dateFromMillis.setValue(null);
        dateToMillis.setValue(null);
    }

    public void loadLogs() {
        String[] params = buildParams();
        LiveData<Resource<List<AuditLogResponse>>> source =
                auditLogRepository.getAuditLogs(params[0], params[1], params[2], params[3], params[4]);
        logsResult.addSource(source, resource -> {
            logsResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                logsResult.removeSource(source);
            }
        });
    }

    public void exportPdf() {
        String[] params = buildParams();
        LiveData<Resource<byte[]>> source =
                auditLogRepository.downloadReportPdf(params[0], params[1], params[2], params[3], params[4]);
        pdfResult.addSource(source, resource -> {
            pdfResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                pdfResult.removeSource(source);
            }
        });
    }

    private String[] buildParams() {
        String actionParam = null;
        String key = actionFilterKey.getValue();
        if (key != null && !FILTER_ACTION_ALL.equals(key)) {
            actionParam = key;
        }

        String userParam = userEmailFilter.getValue();
        String entityParam = entityIdFilter.getValue();

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

        return new String[]{actionParam, userParam, entityParam, fromIso, toIso};
    }
}
