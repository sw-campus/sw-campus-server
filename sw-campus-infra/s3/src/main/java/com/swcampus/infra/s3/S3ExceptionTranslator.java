package com.swcampus.infra.s3;

import org.springframework.stereotype.Component;

import com.swcampus.domain.storage.exception.StorageServiceException;
import com.swcampus.shared.error.GatewayStatusInferrer;
import com.swcampus.shared.error.InfraExceptionTranslator;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * AWS S3 SDK 예외를 도메인 레벨의 StorageServiceException으로 매핑
 */
@Component
public class S3ExceptionTranslator implements InfraExceptionTranslator {
    @Override
    public RuntimeException translate(Throwable t) {
        if (t instanceof StorageServiceException) {
            // 이미 처리된 경우
            return (StorageServiceException) t;
        }
        if (t instanceof S3Exception e) {
            int status = e.statusCode();
            String message = safe(e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage());
            return new StorageServiceException("S3 error: " + message, status);
        }
        if (t instanceof SdkClientException e) {
            int status = GatewayStatusInferrer.inferFromMessage(e.getMessage());
            return new StorageServiceException("S3 client error: " + safe(e.getMessage()), status);
        }
        return null;
    }

    private String safe(String s) { return s == null ? "" : s; }
}
