package com.inspections.repository;

import com.inspections.entity.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceType, String> {
    List<DeviceType> findByEnabledTrueOrderBySortOrderAsc();
    List<DeviceType> findAllByOrderBySortOrderAsc();
}
