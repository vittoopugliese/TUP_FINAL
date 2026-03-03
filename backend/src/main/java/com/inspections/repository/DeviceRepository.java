package com.inspections.repository;

import com.inspections.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByZoneId(String zoneId);
    List<Device> findByLocationId(String locationId);
    List<Device> findByBuildingId(String buildingId);
    List<Device> findByBuildingIdAndDeviceCategory(String buildingId, String deviceCategory);
    List<Device> findByEnabled(boolean enabled);
}
