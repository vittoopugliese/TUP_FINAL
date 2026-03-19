package com.inspections.repository;

import com.inspections.entity.InspectionAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionAssignmentRepository extends JpaRepository<InspectionAssignment, String> {

    List<InspectionAssignment> findByInspectionId(String inspectionId);

    List<InspectionAssignment> findByInspectionIdAndRole(String inspectionId, String role);

    Optional<InspectionAssignment> findByInspectionIdAndUserEmail(String inspectionId, String userEmail);

    void deleteByInspectionIdAndUserEmail(String inspectionId, String userEmail);

    long countByInspectionIdAndRole(String inspectionId, String role);

    boolean existsByInspectionIdAndUserEmail(String inspectionId, String userEmail);

    List<InspectionAssignment> findByUserEmailIgnoreCase(String userEmail);
}
