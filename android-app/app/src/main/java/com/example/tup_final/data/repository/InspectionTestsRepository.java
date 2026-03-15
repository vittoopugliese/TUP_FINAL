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
import com.example.tup_final.data.remote.ZonesApi;
import com.example.tup_final.data.remote.dto.CreateDeviceRequest;
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
    private final DeviceTypesApi deviceTypesApi;
    private final ZoneDao zoneDao;
    private final DeviceDao deviceDao;
    private final TestDao testDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public InspectionTestsRepository(ZonesApi zonesApi, DeviceTypesApi deviceTypesApi,
                                   ZoneDao zoneDao, DeviceDao deviceDao, TestDao testDao) {
        this.zonesApi = zonesApi;
        this.deviceTypesApi = deviceTypesApi;
        this.zoneDao = zoneDao;
        this.deviceDao = deviceDao;
        this.testDao = testDao;
    }

    /**
     * Obtiene zonas con devices y tests para una ubicación e inspección.
     */
    public LiveData<Resource<List<ZoneUiModel>>> getZonesWithDevicesAndTests(
            String locationId, String inspectionId) {
        MutableLiveData<Resource<List<ZoneUiModel>>> result = new MutableLiveData<>();
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

        return result;
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
        for (ZoneWithDevicesResponse z : dtos) {
            ZoneEntity ze = new ZoneEntity(z.getId(), z.getLocationId(), z.getName(), z.getDetails());
            zoneDao.insert(ze);

            if (z.getDevices() != null) {
                for (DeviceWithTestsResponse d : z.getDevices()) {
                    DeviceEntity de = new DeviceEntity();
                    de.id = d.getId();
                    de.zoneId = d.getZoneId();
                    de.locationId = d.getLocationId();
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
