package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.LocationEntity;
import com.example.tup_final.data.entity.LocationWithStats;
import com.example.tup_final.data.local.LocationDao;
import com.example.tup_final.data.remote.LocationApi;
import com.example.tup_final.data.remote.dto.LocationListResponse;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Repository para ubicaciones.
 * Offline-first: intenta API, cachea en Room, fallback a Room.
 * Permite crear nuevas ubicaciones con validación de duplicados.
 */
@Singleton
public class LocationRepository {

    private final LocationDao locationDao;
    private final LocationApi locationApi;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public LocationRepository(LocationDao locationDao, LocationApi locationApi) {
        this.locationDao = locationDao;
        this.locationApi = locationApi;
    }

    /**
     * Obtiene las ubicaciones de un edificio con estadísticas de tests.
     * Offline-first: intenta sincronizar desde API, cachea en Room, fallback a Room.
     * Si buildingId es null o vacío, retorna todas las ubicaciones.
     */
    public LiveData<Resource<List<LocationWithStats>>> getLocationsByBuildingId(String buildingId) {
        MutableLiveData<Resource<List<LocationWithStats>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<LocationListResponse>> response = locationApi.getLocations(
                        (buildingId != null && !buildingId.isEmpty()) ? buildingId : null).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<LocationEntity> entities = mapToEntities(response.body());
                    if (!entities.isEmpty()) {
                        for (LocationEntity e : entities) {
                            locationDao.insert(e);
                        }
                    }
                }
                List<LocationEntity> locations = buildingId != null && !buildingId.isEmpty()
                        ? locationDao.getByBuildingId(buildingId)
                        : locationDao.getAll();
                List<LocationWithStats> withStats = new ArrayList<>();
                for (LocationEntity loc : locations) {
                    int testCount = locationDao.getTestCountForLocation(loc.id);
                    int completedCount = locationDao.getCompletedTestCountForLocation(loc.id);
                    withStats.add(new LocationWithStats(loc, testCount, completedCount));
                }
                mainHandler.post(() -> result.setValue(Resource.success(withStats)));
            } catch (Exception e) {
                try {
                    List<LocationEntity> locations = buildingId != null && !buildingId.isEmpty()
                            ? locationDao.getByBuildingId(buildingId)
                            : locationDao.getAll();
                    List<LocationWithStats> withStats = new ArrayList<>();
                    for (LocationEntity loc : locations) {
                        int testCount = locationDao.getTestCountForLocation(loc.id);
                        int completedCount = locationDao.getCompletedTestCountForLocation(loc.id);
                        withStats.add(new LocationWithStats(loc, testCount, completedCount));
                    }
                    mainHandler.post(() -> result.setValue(Resource.success(withStats)));
                } catch (Exception ex) {
                    mainHandler.post(() -> result.setValue(
                            Resource.error(ex.getMessage() != null ? ex.getMessage() : "Error al cargar ubicaciones")));
                }
            }
        });

        return result;
    }

    private List<LocationEntity> mapToEntities(List<LocationListResponse> dtos) {
        List<LocationEntity> entities = new ArrayList<>();
        for (LocationListResponse dto : dtos) {
            LocationEntity e = new LocationEntity();
            e.id = dto.getId() != null ? dto.getId() : "";
            e.buildingId = dto.getBuildingId();
            e.name = dto.getName();
            e.details = dto.getDetails();
            e.createdAt = null;
            e.updatedAt = null;
            entities.add(e);
        }
        return entities;
    }

    /**
     * Obtiene todas las ubicaciones con estadísticas de tests.
     */
    public LiveData<Resource<List<LocationWithStats>>> getAllLocations() {
        MutableLiveData<Resource<List<LocationWithStats>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                List<LocationEntity> locations = locationDao.getAll();
                List<LocationWithStats> withStats = new ArrayList<>();
                for (LocationEntity loc : locations) {
                    int testCount = locationDao.getTestCountForLocation(loc.id);
                    int completedCount = locationDao.getCompletedTestCountForLocation(loc.id);
                    withStats.add(new LocationWithStats(loc, testCount, completedCount));
                }
                mainHandler.post(() -> result.setValue(Resource.success(withStats)));
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error al cargar ubicaciones")));
            }
        });

        return result;
    }

    /**
     * Crea una nueva ubicación. Valida que el nombre no esté duplicado.
     * Si buildingId se proporciona, la ubicación queda asociada al edificio.
     */
    public LiveData<Resource<LocationEntity>> createLocation(String name, String details, String buildingId) {
        MutableLiveData<Resource<LocationEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                String trimmedName = name != null ? name.trim() : "";
                int dupCount = (buildingId != null && !buildingId.isEmpty())
                        ? locationDao.countByNameAndBuilding(trimmedName, buildingId)
                        : locationDao.countByName(trimmedName);
                if (dupCount > 0) {
                    mainHandler.post(() -> result.setValue(
                            Resource.error("error_name_duplicate")));
                    return;
                }

                LocationEntity entity = new LocationEntity();
                entity.id = UUID.randomUUID().toString();
                entity.buildingId = (buildingId != null && !buildingId.isEmpty()) ? buildingId : null;
                entity.name = trimmedName;
                entity.details = details != null ? details.trim() : null;
                entity.createdAt = null;
                entity.updatedAt = null;

                locationDao.insert(entity);
                mainHandler.post(() -> result.setValue(Resource.success(entity)));
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error al crear ubicación")));
            }
        });

        return result;
    }
}
