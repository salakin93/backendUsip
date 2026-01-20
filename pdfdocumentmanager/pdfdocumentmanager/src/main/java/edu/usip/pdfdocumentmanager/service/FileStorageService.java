package edu.usip.pdfdocumentmanager.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    /**
     * Almacena un archivo usando sourceId como prefijo/identificador y devuelve
     * la ruta interna (storagePath) donde se guardó.
     */
    String storeFile(MultipartFile file, String sourceId) throws IOException;

    /**
     * Recupera el archivo a partir de la ruta interna (storagePath).
     */
    Resource getFile(String storagePath) throws IOException;

    /**
     * Borra el archivo usando la ruta interna (storagePath).
     */
    void deleteFile(String storagePath) throws IOException;
}
