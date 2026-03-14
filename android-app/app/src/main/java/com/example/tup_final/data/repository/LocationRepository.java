package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.LocationEntity;
import com.example.tup_final.data.entity.LocationWithStats;
import com.example.tup_final.data.local.LocationDao;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository para ubicaciones.
 * Carga desde Room y permite crear nuevas ubicaciones con validación de duplicados.
 */
@Singleton
public class LocationRepository {

    private final LocationDao locationDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public LocationRepository(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    /**
     * Obtiene las ubicaciones de un edificio con estadísticas de tests.
     * Si buildingId es null o vacío, retorna todas las ubicaciones.
     */
    public LiveData<Resource<List<LocationWithStats>>> getLocationsByBuildingId(String buildingId) {
        MutableLiveData<Resource<List<LocationWithStats>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
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
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error al cargar ubicaciones")));
            }
        });

        return result;
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
     */
    public LiveData<Resource<LocationEntity>> createLocation(String name, String details) {
        MutableLiveData<Resource<LocationEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                String trimmedName = name != null ? name.trim() : "";
                if (locationDao.countByName(trimmedName) > 0) {
                    mainHandler.post(() -> result.setValue(
                            Resource.error("error_name_duplicate")));
                    return;
                }

                LocationEntity entity = new LocationEntity();
                entity.id = UUID.randomUUID().toString();
                entity.buildingId = null;
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
