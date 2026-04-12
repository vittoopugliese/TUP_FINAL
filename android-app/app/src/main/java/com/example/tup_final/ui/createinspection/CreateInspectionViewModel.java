package com.example.tup_final.ui.createinspection;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.remote.dto.AssignmentRequest;
import com.example.tup_final.data.remote.dto.BuildingListResponse;
import com.example.tup_final.data.remote.dto.BuildingSummaryResponse;
import com.example.tup_final.data.remote.dto.CreateInspectionRequest;
import com.example.tup_final.data.remote.dto.CreateInspectionResponse;
import com.example.tup_final.data.remote.dto.InspectionTemplateListResponse;
import com.example.tup_final.data.repository.CreateInspectionRepository;
import com.example.tup_final.util.Resource;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para el formulario de creación de inspección building-wide.
 */
@HiltViewModel
public class CreateInspectionViewModel extends ViewModel {

    public static final String[] TYPE_OPTIONS = {"Daily", "Weekly", "Monthly", "Annually"};

    /** Claves para {@link Resource#getFormField()} en validación local. */
    public static final String FIELD_BUILDING = "building";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_TEMPLATE = "template";
    public static final String FIELD_INSPECTOR = "inspector";

    private static final String PREFS_ROLE = "cached_role";

    private final CreateInspectionRepository repository;
    private final SharedPreferences prefs;

    private final MediatorLiveData<Resource<List<BuildingListResponse>>> buildingsResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<BuildingSummaryResponse>> buildingSummaryResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<InspectionTemplateListResponse>>> templatesResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<CreateInspectionResponse>> createResult = new MediatorLiveData<>();

    private final MutableLiveData<String> selectedBuildingId = new MutableLiveData<>(null);
    private final MutableLiveData<String> selectedType = new MutableLiveData<>(null);
    private final MutableLiveData<Long> selectedDateMillis = new MutableLiveData<>(null);
    private final MutableLiveData<String> selectedTemplateId = new MutableLiveData<>(null);
    private final MutableLiveData<String> notes = new MutableLiveData<>("");
    private final MutableLiveData<String> inspectorEmail = new MutableLiveData<>("");

    @Inject
    public CreateInspectionViewModel(CreateInspectionRepository repository,
                                     SharedPreferences prefs) {
        this.repository = repository;
        this.prefs = prefs;
        loadBuildings();
        loadTemplates();
        prefillInspectorWithCurrentUser();
    }

    /**
     * Solo el ADMIN elige el inspector en el formulario; el backend asigna al INSPECTOR creador automáticamente.
     */
    public boolean isAdminUser() {
        String r = prefs.getString(PREFS_ROLE, "");
        return r != null && "ADMIN".equalsIgnoreCase(r.trim());
    }

    /**
     * Pre-llena el email del inspector para ADMIN (útil si el admin también inspecciona).
     */
    private void prefillInspectorWithCurrentUser() {
        if (!isAdminUser()) {
            return;
        }
        String email = prefs.getString("cached_email", "");
        if (email != null && !email.trim().isEmpty()) {
            inspectorEmail.setValue(email.trim());
        }
    }

    public void loadBuildings() {
        LiveData<Resource<List<BuildingListResponse>>> source = repository.getBuildings();
        buildingsResult.addSource(source, resource -> {
            buildingsResult.setValue(resource);
            if (resource != null && resource.getStatus() != Resource.Status.LOADING) {
                buildingsResult.removeSource(source);
            }
        });
    }

    public void loadTemplates() {
        LiveData<Resource<List<InspectionTemplateListResponse>>> source = repository.getInspectionTemplates();
        templatesResult.addSource(source, resource -> {
            templatesResult.setValue(resource);
            if (resource != null && resource.getStatus() != Resource.Status.LOADING) {
                templatesResult.removeSource(source);
            }
        });
    }

    public void loadBuildingSummary(String buildingId) {
        if (buildingId == null || buildingId.isEmpty()) {
            buildingSummaryResult.setValue(null);
            return;
        }
        LiveData<Resource<BuildingSummaryResponse>> source = repository.getBuildingSummary(buildingId);
        buildingSummaryResult.addSource(source, resource -> {
            buildingSummaryResult.setValue(resource);
            if (resource != null && resource.getStatus() != Resource.Status.LOADING) {
                buildingSummaryResult.removeSource(source);
            }
        });
    }

    public void setSelectedBuildingId(String buildingId) {
        selectedBuildingId.setValue(buildingId);
        if (buildingId != null && !buildingId.isEmpty()) {
            loadBuildingSummary(buildingId);
        } else {
            buildingSummaryResult.setValue(null);
        }
    }

    public void setSelectedType(String type) {
        selectedType.setValue(type);
    }

    public void setSelectedDateMillis(Long millis) {
        selectedDateMillis.setValue(millis);
    }

    public void setSelectedTemplateId(String templateId) {
        selectedTemplateId.setValue(templateId);
    }

    public void setNotes(String notes) {
        this.notes.setValue(notes != null ? notes : "");
    }

    public void setInspectorEmail(String email) {
        this.inspectorEmail.setValue(email != null ? email : "");
    }

    public void createInspection() {
        String buildingId = selectedBuildingId.getValue();
        String type = selectedType.getValue();
        Long dateMillis = selectedDateMillis.getValue();
        String templateId = selectedTemplateId.getValue();
        String inspector = inspectorEmail.getValue() != null ? inspectorEmail.getValue().trim() : "";

        if (buildingId == null || buildingId.isEmpty()) {
            createResult.setValue(Resource.error("Seleccioná un edificio", FIELD_BUILDING));
            return;
        }
        if (type == null || type.isEmpty()) {
            createResult.setValue(Resource.error("Seleccioná un tipo de inspección", FIELD_TYPE));
            return;
        }
        if (dateMillis == null) {
            createResult.setValue(Resource.error("Seleccioná una fecha programada", FIELD_DATE));
            return;
        }
        if (templateId == null || templateId.isEmpty()) {
            createResult.setValue(Resource.error("Seleccioná una plantilla", FIELD_TEMPLATE));
            return;
        }

        List<AssignmentRequest> assignments;
        if (isAdminUser()) {
            if (inspector.isEmpty()) {
                createResult.setValue(Resource.error("Ingresá el email del inspector", FIELD_INSPECTOR));
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(inspector).matches()) {
                createResult.setValue(Resource.error("Email del inspector inválido", FIELD_INSPECTOR));
                return;
            }
            assignments = new ArrayList<>();
            assignments.add(new AssignmentRequest(inspector, "INSPECTOR"));
        } else {
            // INSPECTOR: el backend ignora asignaciones INSPECTOR del body y asigna al creador;
            // lista vacía cumple el contrato actualizado del API.
            assignments = new ArrayList<>();
        }

        String scheduledDateIso = Instant.ofEpochMilli(dateMillis)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toString();

        CreateInspectionRequest request = new CreateInspectionRequest(
                buildingId,
                type,
                scheduledDateIso,
                templateId,
                notes.getValue() != null && !notes.getValue().trim().isEmpty() ? notes.getValue().trim() : null,
                assignments
        );

        LiveData<Resource<CreateInspectionResponse>> source = repository.createInspection(request);
        createResult.addSource(source, resource -> {
            createResult.setValue(resource);
            if (resource != null && resource.getStatus() != Resource.Status.LOADING) {
                createResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<List<BuildingListResponse>>> getBuildingsResult() {
        return buildingsResult;
    }

    public LiveData<Resource<BuildingSummaryResponse>> getBuildingSummaryResult() {
        return buildingSummaryResult;
    }

    public LiveData<Resource<List<InspectionTemplateListResponse>>> getTemplatesResult() {
        return templatesResult;
    }

    public LiveData<Resource<CreateInspectionResponse>> getCreateResult() {
        return createResult;
    }

    public LiveData<String> getSelectedBuildingId() {
        return selectedBuildingId;
    }

    public LiveData<String> getSelectedType() {
        return selectedType;
    }

    public LiveData<Long> getSelectedDateMillis() {
        return selectedDateMillis;
    }

    public LiveData<String> getSelectedTemplateId() {
        return selectedTemplateId;
    }

    public LiveData<String> getNotes() {
        return notes;
    }

    public LiveData<String> getInspectorEmail() {
        return inspectorEmail;
    }
}
