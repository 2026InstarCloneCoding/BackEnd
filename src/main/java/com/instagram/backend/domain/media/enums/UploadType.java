package com.instagram.backend.domain.media.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UploadType {
    POST("posts"),
    PROFILE("profiles"),
    STORY("stories"),
    MESSAGE("messages");

    private final String directory;

    public static boolean isValid(String value) {
        if (value == null) return false;
        for (UploadType type : values()) {
            if (type.name().equals(value)) return true;//enum.name() : enum-> String
        }
        return false;
    }
}
