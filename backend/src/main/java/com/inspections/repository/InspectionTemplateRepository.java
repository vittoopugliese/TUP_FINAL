package com.inspections.repository;

import com.inspections.entity.InspectionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspectionTemplateRepository extends JpaRepository<InspectionTemplate, String> {
}
