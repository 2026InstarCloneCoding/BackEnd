package com.instagram.backend.global.exception;

import com.instagram.backend.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.name(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
        String code = determineErrorCode(fieldError.getCode(), fieldError.getField());
        String message = fieldError.getDefaultMessage();
        return ResponseEntity
                .status(400)
                .body(ApiResponse.error(code, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(500)
                .body(ApiResponse.error(
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                ));
    }

    private String determineErrorCode(String annotationCode, String field) {
        if (annotationCode == null) return ErrorCode.MISSING_REQUIRED_FIELD.name();
        return switch (annotationCode) {
            case "Email" -> ErrorCode.INVALID_EMAIL_FORMAT.name();
            case "Size", "Min" -> {
                if (field.contains("assword")) yield ErrorCode.INVALID_PASSWORD_FORMAT.name();
                if (field.contains("ontents")) yield ErrorCode.EXCEED_CONTENT_LENGTH.name();
                yield ErrorCode.MISSING_REQUIRED_FIELD.name();
            }
            case "Pattern" -> field.contains("sername") ? ErrorCode.INVALID_USERNAME_FORMAT.name() : ErrorCode.MISSING_REQUIRED_FIELD.name();
            default -> ErrorCode.MISSING_REQUIRED_FIELD.name();
        };
    }
}
