package com.inspections.repository;

import com.inspections.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StepRepository extends JpaRepository<Step, String> {
    List<Step> findByTestId(String testId);
    List<Step> findByTestIdOrderByCreatedAtAsc(String testId);
    List<Step> findByTestIdAndStatus(String testId, String status);
}
