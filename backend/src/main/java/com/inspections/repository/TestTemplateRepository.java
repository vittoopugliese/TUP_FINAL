package com.inspections.repository;

import com.inspections.entity.TestTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestTemplateRepository extends JpaRepository<TestTemplate, String> {
    List<TestTemplate> findByEnabledTrueOrderBySortOrderAsc();
}
