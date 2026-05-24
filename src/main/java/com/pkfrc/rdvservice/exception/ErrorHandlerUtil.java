package com.pkfrc.rdvservice.exception;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ErrorHandlerUtil {

    /**
     * Gestion des exceptions métier
     */
    public static ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        log.warn("Business exception: {} - {}", ex.getCode(), ex.getMessage());

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Erreur métier");
        error.put("code", ex.getCode());
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        error.put("method", request.getMethod());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Gestion des erreurs de validation
     */
    public static ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Erreur de validation");
        error.put("code", "VALIDATION_ERROR");
        error.put("message", "Les données fournies ne sont pas valides");
        error.put("validationErrors", validationErrors);
        error.put("path", request.getRequestURI());
        error.put("method", request.getMethod());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Gestion des erreurs de ressource non trouvée
     */
    public static ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        log.error("Runtime error: ", ex);

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Ressource non trouvée");
        error.put("code", "NOT_FOUND");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        error.put("method", request.getMethod());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Gestion des erreurs génériques
     */
    public static ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error: ", ex);

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Erreur interne du serveur");
        error.put("code", "INTERNAL_ERROR");
        error.put("message", "Une erreur inattendue s'est produite");
        error.put("path", request.getRequestURI());
        error.put("method", request.getMethod());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Gestion des erreurs personnalisée simple
     */
    public static ResponseEntity<Map<String, Object>> createErrorResponse(
            String message,
            String code,
            HttpStatus status,
            HttpServletRequest request) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("code", code);
        error.put("message", message);
        error.put("path", request.getRequestURI());
        error.put("method", request.getMethod());

        return ResponseEntity.status(status).body(error);
    }

    /**
     * Gestion des erreurs 404 simple
     */
    public static ResponseEntity<Map<String, Object>> notFound(
            String resourceName,
            HttpServletRequest request) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Non trouvé");
        error.put("code", "RESOURCE_NOT_FOUND");
        error.put("message", resourceName + " non trouvé");
        error.put("path", request.getRequestURI());
        error.put("method", request.getMethod());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Gestion des erreurs 400 simple
     */
    public static ResponseEntity<Map<String, Object>> badRequest(
            String message,
            HttpServletRequest request) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Requête invalide");
        error.put("code", "BAD_REQUEST");
        error.put("message", message);
        error.put("path", request.getRequestURI());
        error.put("method", request.getMethod());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Gestion des erreurs 409 (Conflit)
     */
    public static ResponseEntity<Map<String, Object>> conflict(
            String message,
            String code,
            HttpServletRequest request) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", HttpStatus.CONFLICT.value());
        error.put("error", "Conflit");
        error.put("code", code);
        error.put("message", message);
        error.put("path", request.getRequestURI());
        error.put("method", request.getMethod());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Gestion des erreurs 500 simple
     */
    public static ResponseEntity<Map<String, Object>> internalError(
            HttpServletRequest request) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Erreur interne");
        error.put("code", "INTERNAL_ERROR");
        error.put("message", "Une erreur interne s'est produite");
        error.put("path", request.getRequestURI());
        error.put("method", request.getMethod());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}