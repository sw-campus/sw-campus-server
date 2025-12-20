package com.swcampus.domain.storage.presigned;

import java.util.List;

import com.swcampus.domain.storage.presigned.PresignedModels.CompletedPart;
import com.swcampus.domain.storage.presigned.PresignedModels.MultipartInitResponse;
import com.swcampus.domain.storage.presigned.PresignedModels.PresignedPart;
import com.swcampus.domain.storage.presigned.PresignedModels.PresignedSingleUpload;

public interface PresignedStoragePort {

    PresignedSingleUpload createSingleUpload(
            String directory,
            String fileName,
            String contentType,
            Long contentLength,
            StorageAccess access
    );

    MultipartInitResponse initiateMultipart(
            String directory,
            String fileName,
            String contentType,
            long totalSize,
            StorageAccess access
    );

    List<PresignedPart> signParts(String key, String uploadId, List<Integer> partNumbers, StorageAccess access);

    void completeMultipart(String key, String uploadId, List<CompletedPart> parts, StorageAccess access);

    void abortMultipart(String key, String uploadId, StorageAccess access);

    void deleteByUrl(String fileUrl, StorageAccess access);
}
