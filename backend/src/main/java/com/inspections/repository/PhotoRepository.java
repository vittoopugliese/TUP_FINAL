package com.inspections.repository;

import com.inspections.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {
    List<Photo> findByDeviceId(String deviceId);
    List<Photo> findByStepId(String stepId);
    List<Photo> findByInspectorId(String inspectorId);
}
