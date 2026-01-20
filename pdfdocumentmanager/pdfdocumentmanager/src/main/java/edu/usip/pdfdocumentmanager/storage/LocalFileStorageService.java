package edu.usip.pdfdocumentmanager.storage;

import edu.usip.pdfdocumentmanager.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path baseDir;

    public LocalFileStorageService(@Value("${file.storage.path:uploads}") String storagePath) {
        this.baseDir = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @Override
    public String storeFile(MultipartFile file, String sourceId) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Archivo vacío o nulo");
        }
        if (sourceId == null || sourceId.isBlank()) {
            throw new RuntimeException("sourceId es requerido");
        }

        Files.createDirectories(baseDir);

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "document.pdf" : file.getOriginalFilename());
        originalName = originalName.replaceAll("[\\\\/]", "_");

        String fileName = sourceId.trim() + "_" + originalName;

        Path target = baseDir.resolve(fileName).normalize();

        if (!target.startsWith(baseDir)) {
            throw new RuntimeException("Ruta inválida para almacenamiento");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return target.toString();
    }

    @Override
    public Resource getFile(String storagePath){
        if (storagePath == null || storagePath.isBlank()) {
            throw new RuntimeException("storagePath es requerido");
        }

        Path filePath = Paths.get(storagePath).toAbsolutePath().normalize();

        if (!filePath.startsWith(baseDir)) {
            throw new RuntimeException("Acceso a archivo fuera del directorio permitido");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new RuntimeException("Archivo no encontrado");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Archivo no legible");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Ruta de archivo inválida", e);
        }
    }

    @Override
    public void deleteFile(String storagePath) throws IOException {
        if (storagePath == null || storagePath.isBlank()) {
            return; // no-op
        }

        Path filePath = Paths.get(storagePath).toAbsolutePath().normalize();

        if (!filePath.startsWith(baseDir)) {
            throw new RuntimeException("Acceso a archivo fuera del directorio permitido");
        }

        Files.deleteIfExists(filePath);
    }
}
