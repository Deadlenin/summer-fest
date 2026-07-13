package com.example.eventplatform.service;

import com.example.eventplatform.config.GalleryProperties;
import com.example.eventplatform.dto.GalleryPhotoResponse;
import com.example.eventplatform.exception.ResourceNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {

    private static final String GALLERY_API_PREFIX = "/api/gallery/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final GalleryProperties galleryProperties;

    @Override
    public List<GalleryPhotoResponse> listPhotos() {
        Path storagePath = getStoragePath();

        try (Stream<Path> files = Files.list(storagePath)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(this::isAllowedImage)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(this::toResponse)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read gallery directory: " + storagePath, exception);
        }
    }

    @Override
    public Resource getPhoto(String filename) {
        validateFilename(filename);

        Path storagePath = getStoragePath();
        Path photoPath = storagePath.resolve(filename).normalize();

        if (!photoPath.startsWith(storagePath)) {
            throw new ResourceNotFoundException("Photo not found: " + filename);
        }

        if (!Files.exists(photoPath) || !Files.isRegularFile(photoPath) || !isAllowedImage(photoPath)) {
            throw new ResourceNotFoundException("Photo not found: " + filename);
        }

        try {
            Resource resource = new UrlResource(photoPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Photo not found: " + filename);
            }
            return resource;
        } catch (IOException exception) {
            throw new ResourceNotFoundException("Photo not found: " + filename);
        }
    }

    private GalleryPhotoResponse toResponse(Path photoPath) {
        String filename = photoPath.getFileName().toString();
        return new GalleryPhotoResponse(filename, GALLERY_API_PREFIX + filename);
    }

    private Path getStoragePath() {
        Path storagePath = Path.of(galleryProperties.storagePath()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(storagePath);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create gallery directory: " + storagePath, exception);
        }

        return storagePath;
    }

    private void validateFilename(String filename) {
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResourceNotFoundException("Photo not found: " + filename);
        }
    }

    private boolean isAllowedImage(Path path) {
        String extension = StringUtils.getFilenameExtension(path.getFileName().toString());
        return extension != null && ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }
}
