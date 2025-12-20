package com.swcampus.api.storage;

import com.swcampus.api.storage.dto.SingleUploadRequest;
import com.swcampus.api.storage.dto.SingleUploadResponse;
import com.swcampus.domain.storage.presigned.PresignedModels.PresignedSingleUpload;
import com.swcampus.domain.storage.presigned.PresignedStoragePort;
import com.swcampus.domain.storage.presigned.StorageAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storage/presigned")
@RequiredArgsConstructor
@Validated
public class StoragePresignedController {

    private final PresignedStoragePort presignedStoragePort;

    @PostMapping("/single")
    public ResponseEntity<SingleUploadResponse> createSingle(
            @RequestBody @Validated SingleUploadRequest req
    ) {
        StorageAccess access = decideAccess(req.getDirectory());

        PresignedSingleUpload res = presignedStoragePort.createSingleUpload(
                sanitizeDirectory(req.getDirectory()),
                req.getFileName(),
                req.getContentType(),
                req.getContentLength(),
                access
        );
        return ResponseEntity.ok(SingleUploadResponse.from(res));
    }

    private StorageAccess decideAccess(String directory) {
        // certificates only PRIVATE; others PUBLIC
        if ("certificates".equalsIgnoreCase(directory)) return StorageAccess.PRIVATE;
        return StorageAccess.PUBLIC;
    }

    private String sanitizeDirectory(String directory) {
        // allow-list known public/private directories; fallback to "banners" for unknown
        if (!"certificates".equalsIgnoreCase(directory)
                && !"lectures".equalsIgnoreCase(directory)
                && !"teachers".equalsIgnoreCase(directory)
                && !"organizations".equalsIgnoreCase(directory)
                && !"banners".equalsIgnoreCase(directory)
                && !"thumbnails".equalsIgnoreCase(directory)
                && !"members".equalsIgnoreCase(directory)) {
            return "banners";
        }
        return directory.toLowerCase();
    }
}
