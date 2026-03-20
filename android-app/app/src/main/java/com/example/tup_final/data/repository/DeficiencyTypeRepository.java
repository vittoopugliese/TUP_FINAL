package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.DeficiencyTypeEntity;
import com.example.tup_final.data.local.DeficiencyTypeDao;
import com.example.tup_final.data.remote.DeficiencyTypeApi;
import com.example.tup_final.data.remote.dto.DeficiencyTypeResponse;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Repositorio del catálogo de tipos de deficiencia.
 * Estrategia: intenta cargar desde API y cachea en Room.
 * Si la API falla, devuelve el catálogo cacheado localmente.
 */
@Singleton
public class DeficiencyTypeRepository {

    private final DeficiencyTypeApi api;
    private final DeficiencyTypeDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public DeficiencyTypeRepository(DeficiencyTypeApi api, DeficiencyTypeDao dao) {
        this.api = api;
        this.dao = dao;
    }

    /**
     * Carga el catálogo: intenta API primero, cachea en Room y publica resultado.
     * Si la API falla, usa el cache de Room.
     *
     * @param result LiveData al que se publica la lista de tipos.
     */
    public void loadDeficiencyTypes(MutableLiveData<Resource<List<DeficiencyTypeEntity>>> result) {
        result.setValue(Resource.loading());
        executor.execute(() -> {
            // Try API first
            try {
                Response<List<DeficiencyTypeResponse>> response = api.getDeficiencyTypes().execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<DeficiencyTypeEntity> entities = mapToEntities(response.body());
                    dao.deleteAll();
                    dao.insertAll(entities);
                    mainHandler.post(() -> result.setValue(Resource.success(entities)));
                    return;
                }
            } catch (Exception ignored) {}

            // Fallback: Room cache
            List<DeficiencyTypeEntity> cached = dao.getAll();
            mainHandler.post(() -> result.setValue(Resource.success(cached)));
        });
    }

    /**
     * Devuelve el catálogo cacheado en Room de forma sincrónica.
     * Debe llamarse desde un hilo de background.
     */
    public List<DeficiencyTypeEntity> getCachedTypes() {
        return dao.getAll();
    }

    private List<DeficiencyTypeEntity> mapToEntities(List<DeficiencyTypeResponse> dtos) {
        List<DeficiencyTypeEntity> list = new ArrayList<>();
        for (DeficiencyTypeResponse dto : dtos) {
            DeficiencyTypeEntity e = new DeficiencyTypeEntity();
            e.id          = dto.id;
            e.code        = dto.code;
            e.name        = dto.name;
            e.description = dto.description;
            e.category    = dto.category;
            list.add(e);
        }
        return list;
    }
}
