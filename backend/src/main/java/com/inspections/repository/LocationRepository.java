package com.inspections.repository;

import com.inspections.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {
    List<Location> findByBuildingId(String buildingId);
    List<Location> findByBuildingIdOrderByNameAsc(String buildingId);
}
