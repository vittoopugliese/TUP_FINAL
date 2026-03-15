package com.inspections.repository;

import com.inspections.entity.DeviceTypeTestTemplate;
import com.inspections.entity.DeviceTypeTestTemplateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceTypeTestTemplateRepository extends JpaRepository<DeviceTypeTestTemplate, DeviceTypeTestTemplateId> {
    List<DeviceTypeTestTemplate> findByDeviceTypeIdOrderBySortOrderAsc(String deviceTypeId);
}
