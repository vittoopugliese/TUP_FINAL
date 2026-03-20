package com.example.tup_final.data.repository;

import com.example.tup_final.data.entity.PhotoEntity;
import com.example.tup_final.data.local.PhotoDao;
import com.example.tup_final.util.PhotoMetadata;

import java.time.Instant;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repositorio para fotos/evidencias.
 * Persiste PhotoEntity en Room con todos los metadatos capturados
 * (timestamp, GPS, inspector). La subida al servidor se realiza
 * en una etapa posterior de sincronización.
 */
@Singleton
public class PhotoRepository {

    private final PhotoDao photoDao;

    @Inject
    public PhotoRepository(PhotoDao photoDao) {
        this.photoDao = photoDao;
    }

    /**
     * Crea y guarda un PhotoEntity a partir de los metadatos capturados.
     *
     * @param metadata  Metadatos de la foto (ruta, timestamp, GPS, inspector).
     * @param stepId    ID del step al que pertenece la foto.
     * @return El ID del PhotoEntity creado, para guardarlo en ObservationEntity.mediaId.
     */
    public String savePhotoWithMetadata(PhotoMetadata metadata, String stepId) {
        String photoId = UUID.randomUUID().toString();

        PhotoEntity entity = new PhotoEntity();
        entity.id = photoId;
        entity.localPath = metadata.localPath;
        entity.timestamp = metadata.timestamp;
        entity.inspectorId = metadata.inspectorId;
        entity.inspectorName = metadata.inspectorName;
        entity.gpsLatitude = metadata.latitude;
        entity.gpsLongitude = metadata.longitude;
        entity.stepId = stepId;
        entity.name = "OBS_" + photoId.substring(0, 8) + ".jpg";
        entity.createdAt = Instant.now().toString();

        if (metadata.latitude != null && metadata.longitude != null) {
            entity.fileDetailsJson = String.format(
                    "{\"lat\":%.6f,\"lon\":%.6f}",
                    metadata.latitude, metadata.longitude);
        }

        photoDao.insert(entity);
        return photoId;
    }

    public PhotoEntity getById(String id) {
        return photoDao.getById(id);
    }
}
