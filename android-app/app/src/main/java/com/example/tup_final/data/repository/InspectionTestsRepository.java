package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.DeviceEntity;
import com.example.tup_final.data.entity.LocationEntity;
import com.example.tup_final.data.entity.TestEntity;
import com.example.tup_final.data.entity.ZoneEntity;
import com.example.tup_final.data.local.DeviceDao;
import com.example.tup_final.data.local.LocationDao;
import com.example.tup_final.data.local.TestDao;
import com.example.tup_final.data.local.ZoneDao;
import com.example.tup_final.data.remote.DeviceTypesApi;
import com.example.tup_final.data.remote.LocationApi;
import com.example.tup_final.data.remote.ZonesApi;
import com.example.tup_final.data.remote.dto.LocationListResponse;
import com.example.tup_final.data.remote.dto.CreateDeviceRequest;
import com.example.tup_final.data.remote.dto.CreateZoneRequest;
import com.example.tup_final.data.remote.dto.DeviceWithTestsResponse;
import com.example.tup_final.data.remote.dto.DeviceTypeResponse;
import com.example.tup_final.data.remote.dto.MoveDeviceRequest;
import com.example.tup_final.data.remote.dto.MoveDeviceResponse;
import com.example.tup_final.data.remote.dto.TestResponse;
import com.example.tup_final.data.remote.dto.ZoneWithDevicesResponse;
import com.example.tup_final.ui.inspectiontests.DeviceUiModel;
import com.example.tup_final.ui.inspectiontests.TestUiModel;
import com.example.tup_final.ui.inspectiontests.ZoneUiModel;
import com.example.tup_final.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Repository para la jerarquía Zonas -> Devices -> Tests.
 * Fetch desde backend, cache en Room, fallback a Room en error.
 */
@Singleton
public class InspectionTestsRepository {

    private static final String TAG = "InspectionTestsRepo";
    private static final int MAX_HTTP_ATTEMPTS = 2;
    private static final int RETRY_DELAY_MS = 750;

    private final ZonesApi zonesApi;
    private final LocationApi locationApi;
    private final DeviceTypesApi deviceTypesApi;
    private final ZoneDao zoneDao;
    private final DeviceDao deviceDao;
    private final TestDao testDao;
    private final LocationDao locationDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public InspectionTestsRepository(ZonesApi zonesApi, LocationApi locationApi,
                                    DeviceTypesApi deviceTypesApi,
                                    ZoneDao zoneDao, DeviceDao deviceDao, TestDao testDao,
                                    LocationDao locationDao) {
        this.zonesApi = zonesApi;
        this.locationApi = locationApi;
        this.deviceTypesApi = deviceTypesApi;
        this.zoneDao = zoneDao;
        this.deviceDao = deviceDao;
        this.testDao = testDao;
        this.locationDao = locationDao;
    }

    /**
     * Obtiene todos los devices de la inspección como lista plana.
     * Prioriza buildingId para alinear con el flujo Locations (building-wide).
     * Solo usa locationId cuando buildingId no está disponible (fallback).
     */
    public LiveData<Resource<List<DeviceEntity>>> getDevicesForInspection(
            String inspectionId, String locationId, String buildingId) {
        MutableLiveData<Resource<List<DeviceEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                List<String> locationIds = new ArrayList<>();
                if (buildingId != null && !buildingId.isEmpty()) {
                    List<String> fetched = fetchLocationIdsWithRetry(buildingId);
                    if (fetched == null) {
                        List<DeviceEntity> fromCache = loadDevicesFromRoom(buildingId, new ArrayList<>());
                        mainHandler.post(() -> result.setValue(fromCache.isEmpty()
                                ? Resource.error("No se pudieron cargar ubicaciones. Verificá tu conexión.")
                                : Resource.success(fromCache)));
                        return;
                    }
                    locationIds = fetched;
                } else if (locationId != null && !locationId.isEmpty()) {
                    locationIds.add(locationId);
                }

                List<DeviceEntity> allDevices = new ArrayList<>();
                boolean anyLocationFailed = false;
                for (String locId : locationIds) {
                    List<ZoneWithDevicesResponse> zones = fetchZonesWithRetry(locId, inspectionId);
                    if (zones != null) {
                        persistToRoom(zones, locId, inspectionId, buildingId);
                        allDevices.addAll(flattenZonesToDevices(zones, buildingId));
                    } else {
                        anyLocationFailed = true;
                    }
                }

                if (anyLocationFailed && allDevices.isEmpty()) {
                    List<DeviceEntity> fromCache = loadDevicesFromRoom(buildingId, locationIds);
                    mainHandler.post(() -> result.setValue(fromCache.isEmpty()
                            ? Resource.error("Error al cargar algunos dispositivos. Verificá tu conexión.")
                            : Resource.success(fromCache)));
                } else {
                    mainHandler.post(() -> result.setValue(Resource.success(allDevices)));
                }
            } catch (Exception e) {
                try {
                    List<DeviceEntity> fromCache = loadDevicesFromRoom(buildingId, new ArrayList<>());
                    mainHandler.post(() -> result.setValue(fromCache.isEmpty()
                            ? Resource.error(e.getMessage() != null ? e.getMessage() : "Error al cargar dispositivos")
                            : Resource.success(fromCache)));
                } catch (Exception ex) {
                    mainHandler.post(() -> result.setValue(Resource.error(
                            e.getMessage() != null ? e.getMessage() : "Error al cargar dispositivos")));
                }
            }
        });

        return result;
    }

    /**
     * Lista de location IDs del edificio; null si ambos intentos HTTP fallan.
     */
    private List<String> fetchLocationIdsWithRetry(String buildingId) {
        for (int attempt = 1; attempt <= MAX_HTTP_ATTEMPTS; attempt++) {
            try {
                Response<List<LocationListResponse>> locResponse =
                        locationApi.getLocations(buildingId).execute();
                if (locResponse.isSuccessful() && locResponse.body() != null) {
                    List<String> ids = new ArrayList<>();
                    for (LocationListResponse loc : locResponse.body()) {
                        if (loc.getId() != null) {
                            ids.add(loc.getId());
                        }
                    }
                    return ids;
                }
                logLocationListFailure(buildingId, attempt, locResponse);
                sleepRetryGap(attempt);
            } catch (Exception e) {
                Log.w(TAG, "fetchLocationIds buildingId=" + buildingId + " attempt=" + attempt, e);
                if (!sleepRetryGap(attempt)) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Zonas con devices para una location; null si ambos intentos fallan.
     */
    private List<ZoneWithDevicesResponse> fetchZonesWithRetry(String locId, String inspectionId) {
        for (int attempt = 1; attempt <= MAX_HTTP_ATTEMPTS; attempt++) {
            try {
                Response<List<ZoneWithDevicesResponse>> response =
                        zonesApi.getZonesWithDevicesAndTests(locId, inspectionId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                }
                logZonesHttpFailure(locId, inspectionId, attempt, response);
                sleepRetryGap(attempt);
            } catch (Exception e) {
                Log.w(TAG, "fetchZones locId=" + locId + " inspectionId=" + inspectionId
                        + " attempt=" + attempt, e);
                if (!sleepRetryGap(attempt)) {
                    return null;
                }
            }
        }
        return null;
    }

    /** @return false si se debe abortar (interrupción). */
    private boolean sleepRetryGap(int attemptCompleted) {
        if (attemptCompleted >= MAX_HTTP_ATTEMPTS) {
            return true;
        }
        try {
            Thread.sleep(RETRY_DELAY_MS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "retry sleep interrupted", e);
            return false;
        }
    }

    private void logLocationListFailure(String buildingId, int attempt,
                                        Response<List<LocationListResponse>> response) {
        StringBuilder sb = new StringBuilder();
        sb.append("getLocations failed buildingId=").append(buildingId).append(" attempt=").append(attempt);
        if (response != null) {
            sb.append(" code=").append(response.code());
            try {
                if (response.errorBody() != null) {
                    sb.append(" errorBody=").append(response.errorBody().string());
                }
            } catch (Exception ignored) {
            }
        }
        Log.w(TAG, sb.toString());
    }

    private void logZonesHttpFailure(String locId, String inspectionId, int attempt,
                                     Response<List<ZoneWithDevicesResponse>> response) {
        StringBuilder sb = new StringBuilder();
        sb.append("getZones failed locId=").append(locId).append(" inspectionId=").append(inspectionId)
                .append(" attempt=").append(attempt);
        if (response != null) {
            sb.append(" code=").append(response.code());
            try {
                if (response.errorBody() != null) {
                    sb.append(" errorBody=").append(response.errorBody().string());
                }
            } catch (Exception ignored) {
            }
        }
        Log.w(TAG, sb.toString());
    }

    private List<DeviceEntity> loadDevicesFromRoom(String buildingId, List<String> locationIds) {
        List<DeviceEntity> list;
        if (buildingId != null && !buildingId.isEmpty()) {
            list = deviceDao.getByBuildingId(buildingId);
        } else if (locationIds != null && !locationIds.isEmpty()) {
            list = new ArrayList<>();
            for (String locId : locationIds) {
                List<DeviceEntity> byLoc = deviceDao.getByLocationId(locId);
                if (byLoc != null) list.addAll(byLoc);
            }
        } else {
            return new ArrayList<>();
        }
        if (list == null) return new ArrayList<>();
        for (DeviceEntity d : list) {
            if (d.zoneId != null) {
                ZoneEntity z = zoneDao.getById(d.zoneId);
                if (z != null) d.zoneName = z.name;
            }
        }
        return list;
    }

    private List<DeviceEntity> flattenZonesToDevices(List<ZoneWithDevicesResponse> zones,
                                                     String buildingId) {
        List<DeviceEntity> result = new ArrayList<>();
        for (ZoneWithDevicesResponse z : zones) {
            if (z.getDevices() != null) {
                for (DeviceWithTestsResponse d : z.getDevices()) {
                    DeviceEntity de = new DeviceEntity();
                    de.id = d.getId();
                    de.zoneId = d.getZoneId();
                    de.locationId = d.getLocationId();
                    de.buildingId = buildingId;
                    de.zoneName = z.getName();
                    de.name = d.getName();
                    de.deviceCategory = d.getDeviceCategory();
                    de.deviceSerialNumber = d.getDeviceSerialNumber();
                    de.enabled = d.isEnabled();
                    result.add(de);
                }
            }
        }
        return result;
    }

    /**
     * Carga zonas con devices y tests. Actualiza el MutableLiveData pasado.
     */
    public void loadZonesWithDevicesAndTests(String locationId, String inspectionId,
                                            MutableLiveData<Resource<List<ZoneUiModel>>> result) {
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<ZoneWithDevicesResponse>> response =
                        zonesApi.getZonesWithDevicesAndTests(locationId, inspectionId).execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<ZoneUiModel> zones = mapToUiModels(response.body(), inspectionId);
                    persistToRoom(response.body(), locationId, inspectionId);
                    mainHandler.post(() -> result.setValue(Resource.success(zones)));
                } else {
                    List<ZoneUiModel> fromCache = loadFromRoom(locationId, inspectionId);
                    mainHandler.post(() -> result.setValue(Resource.success(fromCache)));
                }
            } catch (Exception e) {
                try {
                    List<ZoneUiModel> fromCache = loadFromRoom(locationId, inspectionId);
                    mainHandler.post(() -> result.setValue(Resource.success(fromCache)));
                } catch (Exception ex) {
                    mainHandler.post(() -> result.setValue(Resource.error(
                            ex.getMessage() != null ? ex.getMessage() : "Error al cargar zonas")));
                }
            }
        });
    }

    /**
     * Crea una zona en la ubicación indicada.
     */
    public void createZone(String locationId, CreateZoneRequest request,
                          MutableLiveData<Resource<ZoneUiModel>> result) {
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<ZoneWithDevicesResponse> response =
                        zonesApi.createZone(locationId, request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    ZoneWithDevicesResponse z = response.body();
                    String zLoc = z.getLocationId() != null ? z.getLocationId() : locationId;
                    ensureLocationInRoom(zLoc, null);
                    ZoneEntity ze = new ZoneEntity(z.getId(), zLoc, z.getName(), z.getDetails());
                    zoneDao.insert(ze);

                    ZoneUiModel model = new ZoneUiModel(
                            z.getId(), z.getLocationId(), z.getName(), z.getDetails(),
                            new ArrayList<>());
                    mainHandler.post(() -> result.setValue(Resource.success(model)));
                } else {
                    String msg = "Error al crear zona";
                    try {
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    final String errMsg = msg;
                    mainHandler.post(() -> result.setValue(Resource.error(errMsg)));
                }
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Error de conexión";
                mainHandler.post(() -> result.setValue(Resource.error(msg)));
            }
        });
    }

    /**
     * Crea un dispositivo en la zona indicada.
     * Actualiza el MutableLiveData con el resultado.
     */
    public void createDevice(String locationId, String zoneId, CreateDeviceRequest request,
                            MutableLiveData<Resource<DeviceUiModel>> result) {
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<DeviceWithTestsResponse> response =
                        zonesApi.createDevice(locationId, zoneId, request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    DeviceWithTestsResponse d = response.body();
                    List<TestUiModel> tests = new ArrayList<>();
                    if (d.getTests() != null) {
                        for (TestResponse t : d.getTests()) {
                            tests.add(new TestUiModel(
                                    t.getId(), t.getDeviceId(), t.getInspectionId(),
                                    t.getName(), t.getDescription(), t.getStatus()));
                        }
                    }
                    DeviceUiModel model = new DeviceUiModel(
                            d.getId(), d.getZoneId(), d.getLocationId(),
                            d.getName(), d.getDeviceCategory(), d.getDeviceSerialNumber(),
                            d.isEnabled(), tests);
                    mainHandler.post(() -> result.setValue(Resource.success(model)));
                } else {
                    String msg = "Error al crear dispositivo";
                    try {
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    final String errMsg = msg;
                    mainHandler.post(() -> result.setValue(Resource.error(errMsg)));
                }
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Error de conexión";
                mainHandler.post(() -> result.setValue(Resource.error(msg)));
            }
        });
    }

    /**
     * Mueve un dispositivo a otra zona dentro de la misma ubicación.
     */
    public void moveDevice(String locationId, String deviceId, MoveDeviceRequest request,
                           MutableLiveData<Resource<MoveDeviceResponse>> result) {
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<MoveDeviceResponse> response =
                        zonesApi.moveDevice(locationId, deviceId, request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    String msg = "Error al mover dispositivo";
                    try {
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    final String errMsg = msg;
                    mainHandler.post(() -> result.setValue(Resource.error(errMsg)));
                }
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Error de conexión";
                mainHandler.post(() -> result.setValue(Resource.error(msg)));
            }
        });
    }

    /**
     * Obtiene el catálogo de tipos de dispositivo.
     */
    public void loadDeviceTypes(MutableLiveData<Resource<List<DeviceTypeResponse>>> result) {
        result.setValue(Resource.loading());
        executor.execute(() -> {
            try {
                Response<List<DeviceTypeResponse>> response =
                        deviceTypesApi.getDeviceTypes(false).execute();
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    mainHandler.post(() -> result.setValue(Resource.error("Error al cargar tipos")));
                }
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(Resource.error(
                        e.getMessage() != null ? e.getMessage() : "Error de conexión")));
            }
        });
    }

    private List<ZoneUiModel> mapToUiModels(List<ZoneWithDevicesResponse> dtos, String inspectionId) {
        List<ZoneUiModel> result = new ArrayList<>();
        for (ZoneWithDevicesResponse z : dtos) {
            List<DeviceUiModel> devices = new ArrayList<>();
            if (z.getDevices() != null) {
                for (DeviceWithTestsResponse d : z.getDevices()) {
                    List<TestUiModel> tests = new ArrayList<>();
                    if (d.getTests() != null) {
                        for (TestResponse t : d.getTests()) {
                            if (inspectionId == null || inspectionId.equals(t.getInspectionId())) {
                                tests.add(new TestUiModel(
                                        t.getId(), t.getDeviceId(), t.getInspectionId(),
                                        t.getName(), t.getDescription(), t.getStatus()));
                            }
                        }
                    }
                    devices.add(new DeviceUiModel(
                            d.getId(), d.getZoneId(), d.getLocationId(),
                            d.getName(), d.getDeviceCategory(), d.getDeviceSerialNumber(),
                            d.isEnabled(), tests));
                }
            }
            result.add(new ZoneUiModel(
                    z.getId(), z.getLocationId(), z.getName(), z.getDetails(), devices));
        }
        return result;
    }

    private void persistToRoom(List<ZoneWithDevicesResponse> dtos,
                               String locationId, String inspectionId) {
        persistToRoom(dtos, locationId, inspectionId, null);
    }

    private void persistToRoom(List<ZoneWithDevicesResponse> dtos,
                               String locationId, String inspectionId, String buildingId) {
        ensureLocationInRoom(locationId, buildingId);
        for (ZoneWithDevicesResponse z : dtos) {
            String zLocId = z.getLocationId() != null && !z.getLocationId().isEmpty()
                    ? z.getLocationId() : locationId;
            ensureLocationInRoom(zLocId, buildingId);
            ZoneEntity ze = new ZoneEntity(z.getId(), zLocId, z.getName(), z.getDetails());
            zoneDao.upsert(ze);

            if (z.getDevices() != null) {
                for (DeviceWithTestsResponse d : z.getDevices()) {
                    String dLocId = d.getLocationId() != null && !d.getLocationId().isEmpty()
                            ? d.getLocationId() : zLocId;
                    ensureLocationInRoom(dLocId, buildingId);
                    DeviceEntity de = new DeviceEntity();
                    de.id = d.getId();
                    de.zoneId = d.getZoneId();
                    de.locationId = dLocId;
                    de.buildingId = buildingId;
                    de.name = d.getName();
                    de.deviceCategory = d.getDeviceCategory();
                    de.deviceSerialNumber = d.getDeviceSerialNumber();
                    de.enabled = d.isEnabled();
                    deviceDao.upsert(de);

                    if (d.getTests() != null) {
                        for (TestResponse t : d.getTests()) {
                            if (inspectionId != null && inspectionId.equals(t.getInspectionId())) {
                                TestEntity te = new TestEntity();
                                te.id = t.getId();
                                te.deviceId = t.getDeviceId();
                                te.inspectionId = t.getInspectionId();
                                te.name = t.getName();
                                te.description = t.getDescription();
                                te.status = t.getStatus();
                                testDao.upsert(te);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * ZoneEntity y DeviceEntity tienen FK a locations.id; sin esta fila, upsert falla con SQLITE_CONSTRAINT_FOREIGNKEY.
     */
    private void ensureLocationInRoom(String locId, String buildingId) {
        if (locId == null || locId.isEmpty()) {
            return;
        }
        if (locationDao.getById(locId) != null) {
            return;
        }
        LocationEntity le = new LocationEntity();
        le.id = locId;
        le.buildingId = buildingId;
        le.name = "";
        le.details = null;
        le.createdAt = null;
        le.updatedAt = null;
        locationDao.insert(le);
    }

    private List<ZoneUiModel> loadFromRoom(String locationId, String inspectionId) {
        List<ZoneEntity> zones = zoneDao.getByLocationId(locationId);
        List<ZoneUiModel> result = new ArrayList<>();

        for (ZoneEntity z : zones) {
            List<DeviceEntity> devices = deviceDao.getByZoneId(z.id);
            List<DeviceUiModel> deviceModels = new ArrayList<>();

            for (DeviceEntity d : devices) {
                List<TestEntity> tests = testDao.getByDeviceId(d.id);
                List<TestUiModel> testModels = new ArrayList<>();
                for (TestEntity t : tests) {
                    if (inspectionId == null || inspectionId.equals(t.inspectionId)) {
                        testModels.add(new TestUiModel(
                                t.id, t.deviceId, t.inspectionId,
                                t.name, t.description, t.status));
                    }
                }
                deviceModels.add(new DeviceUiModel(
                        d.id, d.zoneId, d.locationId, d.name,
                        d.deviceCategory, d.deviceSerialNumber, d.enabled, testModels));
            }

            result.add(new ZoneUiModel(z.id, z.locationId, z.name, z.details, deviceModels));
        }

        return result;
    }
}
