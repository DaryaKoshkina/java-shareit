package ru.practicum.shareit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.function.Supplier;

public abstract class BaseClient {
    protected static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    protected BaseClient(String baseUrl, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    protected <T> T get(String path, Long userId, Class<T> type) {
        return execute(() -> restClient.get()
                .uri(path)
                .headers(headers -> addUserHeader(headers, userId))
                .retrieve()
                .body(type));
    }

    protected <T> T get(String path, Long userId, ParameterizedTypeReference<T> type) {
        return execute(() -> restClient.get()
                .uri(path)
                .headers(headers -> addUserHeader(headers, userId))
                .retrieve()
                .body(type));
    }

    protected <T> T get(String path, Long userId, ParameterizedTypeReference<T> type, Object... uriVariables) {
        return execute(() -> restClient.get()
                .uri(path, uriVariables)
                .headers(headers -> addUserHeader(headers, userId))
                .retrieve()
                .body(type));
    }

    protected <T> T post(String path, Long userId, Object body, Class<T> type) {
        return execute(() -> restClient.post()
                .uri(path)
                .headers(headers -> addUserHeader(headers, userId))
                .body(body)
                .retrieve()
                .body(type));
    }

    protected <T> T patch(String path, Long userId, Object body, Class<T> type) {
        return execute(() -> restClient.patch()
                .uri(path)
                .headers(headers -> addUserHeader(headers, userId))
                .body(body)
                .retrieve()
                .body(type));
    }

    protected <T> T patch(String path, Long userId, Class<T> type) {
        return execute(() -> restClient.patch()
                .uri(path)
                .headers(headers -> addUserHeader(headers, userId))
                .retrieve()
                .body(type));
    }

    protected void delete(String path) {
        execute(() -> restClient.delete()
                .uri(path)
                .retrieve()
                .toBodilessEntity());
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientResponseException e) {
            throw mapException(e);
        }
    }

    private RuntimeException mapException(RestClientResponseException e) {
        String message = extractMessage(e.getResponseBodyAsString());
        return switch (e.getStatusCode().value()) {
            case 400 -> new BadRequestException(message);
            case 403 -> new ForbiddenException(message);
            case 404 -> new NotFoundException(message);
            case 409 -> new ConflictException(message);
            default -> new RuntimeException(message, e);
        };
    }

    private String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return "Ошибка при обращении к серверу";
        }
        try {
            ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
            if (errorResponse.getError() != null && !errorResponse.getError().isBlank()) {
                return errorResponse.getError();
            }
        } catch (Exception ignored) {
            return body;
        }
        return body;
    }

    private void addUserHeader(HttpHeaders headers, Long userId) {
        if (userId != null) {
            headers.set(USER_ID_HEADER, String.valueOf(userId));
        }
    }
}
