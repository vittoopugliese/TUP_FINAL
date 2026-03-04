package com.example.tup_final.data.remote;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Base API interface for the Inspections system.
 * Endpoints will be added here as the backend is implemented.
 */
public interface ApiService {
    
    @GET("health")
    Call<Void> checkHealth();
    
    // Auth endpoints (as implemented in the current backend)
    // POST /api/auth/login
    // POST /api/auth/logout
    // POST /api/auth/refresh
}
