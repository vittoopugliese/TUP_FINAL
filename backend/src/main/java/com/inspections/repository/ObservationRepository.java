package com.inspections.repository;

import com.inspections.entity.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservationRepository extends JpaRepository<Observation, String> {
    List<Observation> findByTestStepId(String testStepId);
    List<Observation> findByInspectionId(String inspectionId);
    List<Observation> findByInspectionIdAndType(String inspectionId, String type);
}
