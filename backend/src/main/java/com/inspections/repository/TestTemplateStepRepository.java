package com.inspections.repository;

import com.inspections.entity.TestTemplateStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for test template step definitions.
 */
public interface TestTemplateStepRepository extends JpaRepository<TestTemplateStep, String> {

    List<TestTemplateStep> findByTestTemplateIdOrderBySortOrderAsc(String testTemplateId);
}
