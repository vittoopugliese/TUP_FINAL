package com.inspections.repository;

import com.inspections.entity.InspectionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<InspectionTest, String> {
    List<InspectionTest> findByInspectionId(String inspectionId);
    List<InspectionTest> findByDeviceId(String deviceId);
    List<InspectionTest> findByInspectionIdAndStatus(String inspectionId, String status);
    List<InspectionTest> findByDeviceIdAndInspectionId(String deviceId, String inspectionId);

    boolean existsByDeviceIdAndInspectionIdAndTestTemplateId(String deviceId, String inspectionId, String testTemplateId);
}
