package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.AuditLogResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AuditLogApi {

    @GET("audit-logs")
    Call<List<AuditLogResponse>> getAuditLogs(
            @Query("action") String action,
            @Query("userId") String userId,
            @Query("entityId") String entityId,
            @Query("from") String from,
            @Query("to") String to);

    @GET("audit-logs/report/pdf")
    Call<ResponseBody> getAuditLogReportPdf(
            @Query("action") String action,
            @Query("userId") String userId,
            @Query("entityId") String entityId,
            @Query("from") String from,
            @Query("to") String to);
}
