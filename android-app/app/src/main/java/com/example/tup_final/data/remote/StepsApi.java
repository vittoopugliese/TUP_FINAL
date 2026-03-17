package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.StepResponse;
import com.example.tup_final.data.remote.dto.UpdateStepRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

/**
 * API para steps de tests.
 * Base URL: /api/
 *
 * GET /api/tests/{testId}/steps
 * PATCH /api/steps/{stepId}
 */
public interface StepsApi {

    @GET("tests/{testId}/steps")
    Call<List<StepResponse>> getStepsByTestId(@Path("testId") String testId);

    @PATCH("steps/{stepId}")
    Call<StepResponse> updateStep(@Path("stepId") String stepId, @Body UpdateStepRequest request);
}
