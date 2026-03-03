package com.inspections.repository;

import com.inspections.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, String> {
    List<Zone> findByLocationId(String locationId);
    List<Zone> findByLocationIdOrderByNameAsc(String locationId);
}
