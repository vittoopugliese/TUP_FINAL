package com.example.tup_final.data.repository;

import com.example.tup_final.data.entity.PhotoEntity;
import com.example.tup_final.data.local.PhotoDao;
import com.example.tup_final.util.PhotoMetadata;

import java.time.Instant;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repositorio de fotos.
 * Persiste {@link PhotoEntity} en Room con todos los metadatos capturados
 * (GPS, timestamp, inspector). Llamado desde ObservationRepository en el
 * hilo de background del executor.
 */
@Singleton
public class PhotoRepository {

    private final PhotoDao photoDao;

    @Inject
    public PhotoRepository(PhotoDao photoDao) {
        this.photoDao = photoDao;
    }

    /**
     * Guarda la foto con metadatos completos en Room.
     * Debe llamarse desde un hilo de background.
     *
     * @param metadata Metadatos capturados junto a la foto.
     * @param stepId   ID del step al que pertenece la foto.
     * @return ID único (UUID) de la entidad guardada. Úsalo como {@code mediaId} en la observación.
     */
    public String savePhotoWithMetadata(PhotoMetadata metadata, String stepId) {
        String id  = UUID.randomUUID().toString();
        String now = Instant.now().toString();

        PhotoEntity entity = new PhotoEntity();
        entity.id           = id;
        entity.localPath    = metadata.localPath;
        entity.timestamp    = metadata.timestamp;
        entity.inspectorId  = metadata.inspectorId;
        entity.inspectorName= metadata.inspectorName;
        entity.gpsLatitude  = metadata.latitude;
        entity.gpsLongitude = metadata.longitude;
        entity.stepId       = stepId;
        entity.createdAt    = now;

        photoDao.insert(entity);
        return id;
    }

    /** Obtiene una entidad por ID (síncrono, desde background). */
    public PhotoEntity getById(String id) {
        return photoDao.getById(id);
    }
}
