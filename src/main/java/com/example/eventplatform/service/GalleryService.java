package com.example.eventplatform.service;

import com.example.eventplatform.dto.GalleryPhotoResponse;
import java.util.List;
import org.springframework.core.io.Resource;

public interface GalleryService {

    List<GalleryPhotoResponse> listPhotos();

    Resource getPhoto(String filename);
}
