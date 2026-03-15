package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.remote.BuildingApi;
import com.example.tup_final.data.remote.InspectionApi;
import com.example.tup_final.data.remote.InspectionTemplateApi;
import com.example.tup_final.data.remote.dto.BuildingListResponse;
import com.example.tup_final.data.remote.dto.BuildingSummaryResponse;
import com.example.tup_final.data.remote.dto.CreateInspectionRequest;
import com.example.tup_final.data.remote.dto.CreateInspectionResponse;
import com.example.tup_final.data.remote.dto.InspectionTemplateListResponse;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Repository para el flujo de creación de inspecciones.
 */
@Singleton
public class CreateInspectionRepository {

    private final BuildingApi buildingApi;
    private final InspectionTemplateApi inspectionTemplateApi;
    private final InspectionApi inspectionApi;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public CreateInspectionRepository(BuildingApi buildingApi,
                                     InspectionTemplateApi inspectionTemplateApi,
                                     InspectionApi inspectionApi) {
        this.buildingApi = buildingApi;
        this.inspectionTemplateApi = inspectionTemplateApi;
        this.inspectionApi = inspectionApi;
    }

    public LiveData<Resource<List<BuildingListResponse>>> getBuildings() {
        MutableLiveData<Resource<List<BuildingListResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<BuildingListResponse>> response = buildingApi.getBuildings().execute();
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    String msg = response.errorBody() != null ? response.errorBody().string() : "Error al cargar edificios";
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error de conexión")));
            }
        });

        return result;
    }

    public LiveData<Resource<BuildingSummaryResponse>> getBuildingSummary(String buildingId) {
        MutableLiveData<Resource<BuildingSummaryResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<BuildingSummaryResponse> response = buildingApi.getBuildingSummary(buildingId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    String msg = response.errorBody() != null ? response.errorBody().string() : "Error al cargar resumen";
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error de conexión")));
            }
        });

        return result;
    }

    public LiveData<Resource<List<InspectionTemplateListResponse>>> getInspectionTemplates() {
        MutableLiveData<Resource<List<InspectionTemplateListResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<InspectionTemplateListResponse>> response = inspectionTemplateApi.getTemplates().execute();
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    String msg = response.errorBody() != null ? response.errorBody().string() : "Error al cargar plantillas";
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error de conexión")));
            }
        });

        return result;
    }

    public LiveData<Resource<CreateInspectionResponse>> createInspection(CreateInspectionRequest request) {
        MutableLiveData<Resource<CreateInspectionResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<CreateInspectionResponse> response = inspectionApi.createInspection(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    String msg = response.errorBody() != null ? response.errorBody().string() : "Error al crear inspección";
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error de conexión")));
            }
        });

        return result;
    }
}
