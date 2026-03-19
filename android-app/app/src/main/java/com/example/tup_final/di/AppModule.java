package com.example.tup_final.di;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.tup_final.data.remote.ApiService;
import com.example.tup_final.data.remote.AuthApi;
import com.example.tup_final.data.remote.AuthInterceptor;
import com.example.tup_final.data.remote.BuildingApi;
import com.example.tup_final.data.remote.InspectionApi;
import com.example.tup_final.data.remote.InspectionTemplateApi;
import com.example.tup_final.data.remote.LocationApi;
import com.example.tup_final.data.remote.DeviceTypesApi;
import com.example.tup_final.data.remote.ObservationApi;
import com.example.tup_final.data.remote.StepsApi;
import com.example.tup_final.data.remote.ZonesApi;
import com.example.tup_final.data.remote.UserApi;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * Base Hilt module for application-level dependencies.
 * Add providers here as needed (e.g. Database, Retrofit, SharedPreferences).
 */
@Module
@InstallIn(SingletonComponent.class)
public final class AppModule {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(
            HttpLoggingInterceptor loggingInterceptor,
            AuthInterceptor authInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    public ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    @Provides
    @Singleton
    public AuthApi provideAuthApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
    }

    @Provides
    @Singleton
    public UserApi provideUserApi(Retrofit retrofit) {
        return retrofit.create(UserApi.class);
    }

    @Provides
    @Singleton
    public InspectionApi provideInspectionApi(Retrofit retrofit) {
        return retrofit.create(InspectionApi.class);
    }

    @Provides
    @Singleton
    public BuildingApi provideBuildingApi(Retrofit retrofit) {
        return retrofit.create(BuildingApi.class);
    }

    @Provides
    @Singleton
    public InspectionTemplateApi provideInspectionTemplateApi(Retrofit retrofit) {
        return retrofit.create(InspectionTemplateApi.class);
    }

    @Provides
    @Singleton
    public LocationApi provideLocationApi(Retrofit retrofit) {
        return retrofit.create(LocationApi.class);
    }

    @Provides
    @Singleton
    public ZonesApi provideZonesApi(Retrofit retrofit) {
        return retrofit.create(ZonesApi.class);
    }

    @Provides
    @Singleton
    public DeviceTypesApi provideDeviceTypesApi(Retrofit retrofit) {
        return retrofit.create(DeviceTypesApi.class);
    }

    @Provides
    @Singleton
    public StepsApi provideStepsApi(Retrofit retrofit) {
        return retrofit.create(StepsApi.class);
    }

    @Provides
    @Singleton
    public ObservationApi provideObservationApi(Retrofit retrofit) {
        return retrofit.create(ObservationApi.class);
    }

    @Provides
    @Singleton
    public SharedPreferences provideAuthSharedPreferences(@ApplicationContext Context context) {
        return context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    public AuthInterceptor provideAuthInterceptor(SharedPreferences authPrefs) {
        return new AuthInterceptor(authPrefs);
    }
}
