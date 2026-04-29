package com.instagram.backend.domain.media.service;

import com.instagram.backend.domain.media.dto.request.PresignedUrlRequest;
import com.instagram.backend.domain.media.dto.response.PresignedUrlResponse;
import com.instagram.backend.domain.media.enums.UploadType;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${media.presigned-url-expiration}")
    private long expiresIn;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10_485_760L; // 10MB

    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        if (!ALLOWED_TYPES.contains(request.getFileType())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
        if (request.getFileSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.EXCEED_FILE_SIZE);
        }
        if (!UploadType.isValid(request.getUploadType())) {
            throw new BusinessException(ErrorCode.INVALID_UPLOAD_TYPE);
        }

        UploadType uploadType = UploadType.valueOf(request.getUploadType());
        String uuid = UUID.randomUUID().toString();
        String objectKey = uploadType.getDirectory() + "/" + uuid + "_" + request.getFileName();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expiresIn))
                .putObjectRequest(r -> r
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType(request.getFileType())
                )
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        String mediaUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + objectKey;

        return PresignedUrlResponse.builder()
                .uploadUrl(presigned.url().toString())
                .mediaUrl(mediaUrl)
                .mediaUuid(uuid)
                .expiresIn(expiresIn)
                .build();
    }
}
