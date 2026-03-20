package com.inspections.repository;

import com.inspections.entity.DeficiencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeficiencyTypeRepository extends JpaRepository<DeficiencyType, String> {
    List<DeficiencyType> findByEnabledTrueOrderBySortOrderAsc();
    List<DeficiencyType> findAllByOrderBySortOrderAsc();
}
