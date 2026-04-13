package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.remote.AuditLogApi;
import com.example.tup_final.data.remote.dto.AuditLogResponse;
import com.example.tup_final.util.Resource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Lista registros de auditoría (solo ADMIN en backend).
 */
@Singleton
public class AuditLogRepository {

    private final AuditLogApi auditLogApi;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public AuditLogRepository(AuditLogApi auditLogApi) {
        this.auditLogApi = auditLogApi;
    }

    public LiveData<Resource<List<AuditLogResponse>>> getAuditLogs(String action, String userId,
                                                                    String entityId, String fromIso, String toIso) {
        MutableLiveData<Resource<List<AuditLogResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<AuditLogResponse>> response =
                        auditLogApi.getAuditLogs(action, userId, entityId, fromIso, toIso).execute();
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    String msg = parseErrorMessage(response.errorBody());
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (IOException e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error("Error de conexión. Verificá tu internet.")));
            }
        });

        return result;
    }

    public LiveData<Resource<byte[]>> downloadReportPdf(String action, String userId,
                                                         String entityId, String fromIso, String toIso) {
        MutableLiveData<Resource<byte[]>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<ResponseBody> response =
                        auditLogApi.getAuditLogReportPdf(action, userId, entityId, fromIso, toIso).execute();
                if (response.isSuccessful() && response.body() != null) {
                    byte[] bytes = response.body().bytes();
                    mainHandler.post(() -> result.setValue(Resource.success(bytes)));
                } else {
                    String msg = parseErrorMessage(response.errorBody());
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (IOException e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error("Error de conexión. Verificá tu internet.")));
            }
        });

        return result;
    }

    private String parseErrorMessage(ResponseBody errorBody) {
        if (errorBody == null) return "Error desconocido";
        try {
            return errorBody.string();
        } catch (IOException e) {
            return "Error desconocido";
        }
    }
}
