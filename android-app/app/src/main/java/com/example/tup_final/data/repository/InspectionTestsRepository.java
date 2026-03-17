package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.DeviceEntity;
import com.example.tup_final.data.entity.TestEntity;
import com.example.tup_final.data.entity.ZoneEntity;
import com.example.tup_final.data.local.DeviceDao;
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

    private final ZonesApi zonesApi;
    private final LocationApi locationApi;
    private final DeviceTypesApi deviceTypesApi;
    private final ZoneDao zoneDao;
    private final DeviceDao deviceDao;
    private final TestDao testDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public InspectionTestsRepository(ZonesApi zonesApi, LocationApi locationApi,
                                    DeviceTypesApi deviceTypesApi,
                                    ZoneDao zoneDao, DeviceDao deviceDao, TestDao testDao) {
        this.zonesApi = zonesApi;
        this.locationApi = locationApi;
        this.deviceTypesApi = deviceTypesApi;
        this.zoneDao = zoneDao;
        this.deviceDao = deviceDao;
        this.testDao = testDao;
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
                    Response<List<LocationListResponse>> locResponse =
                            locationApi.getLocations(buildingId).execute();
                    if (locResponse.isSuccessful() && locResponse.body() != null) {
                        for (LocationListResponse loc : locResponse.body()) {
                            if (loc.getId() != null) {
                                locationIds.add(loc.getId());
                            }
                        }
                    } else {
                        List<DeviceEntity> fromCache = loadDevicesFromRoom(buildingId, new ArrayList<>());
                        mainHandler.post(() -> result.setValue(fromCache.isEmpty()
                                ? Resource.error("No se pudieron cargar ubicaciones. Verificá tu conexión.")
                                : Resource.success(fromCache)));
                        return;
                    }
                } else if (locationId != null && !locationId.isEmpty()) {
                    locationIds.add(locationId);
                }

                List<DeviceEntity> allDevices = new ArrayList<>();
                boolean anyLocationFailed = false;
                for (String locId : locationIds) {
                    try {
                        Response<List<ZoneWithDevicesResponse>> response =
                                zonesApi.getZonesWithDevicesAndTests(locId, inspectionId).execute();
                        if (response.isSuccessful() && response.body() != null) {
                            List<ZoneWithDevicesResponse> zones = response.body();
                            persistToRoom(zones, locId, inspectionId, buildingId);
                            List<DeviceEntity> flat = flattenZonesToDevices(zones, buildingId);
                            allDevices.addAll(flat);
                        } else {
                            anyLocationFailed = true;
                        }
                    } catch (Exception ignored) {
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
                    ZoneEntity ze = new ZoneEntity(z.getId(), z.getLocationId(), z.getName(), z.getDetails());
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
        for (ZoneWithDevicesResponse z : dtos) {
            ZoneEntity ze = new ZoneEntity(z.getId(), z.getLocationId(), z.getName(), z.getDetails());
            zoneDao.insert(ze);

            if (z.getDevices() != null) {
                for (DeviceWithTestsResponse d : z.getDevices()) {
                    DeviceEntity de = new DeviceEntity();
                    de.id = d.getId();
                    de.zoneId = d.getZoneId();
                    de.locationId = d.getLocationId();
                    de.buildingId = buildingId;
                    de.name = d.getName();
                    de.deviceCategory = d.getDeviceCategory();
                    de.deviceSerialNumber = d.getDeviceSerialNumber();
                    de.enabled = d.isEnabled();
                    deviceDao.insert(de);

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
                                testDao.insert(te);
                            }
                        }
                    }
                }
            }
        }
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
