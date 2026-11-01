package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Component
public class ItemRequestClient extends BaseClient {

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.rootUri(serverUrl).build());
    }

    public ResponseEntity<Object> create(Long userId, ItemRequestDto itemRequestDto) {
        return post("/requests", userId, itemRequestDto);
    }

    public ResponseEntity<Object> getOwn(Long userId) {
        return get("/requests", userId, null);
    }

    public ResponseEntity<Object> getAll(Long userId, int from, int size) {
        return get("/requests/all?from={from}&size={size}", userId, Map.of("from", from, "size", size));
    }

    public ResponseEntity<Object> getById(Long userId, Long requestId) {
        return get("/requests/{requestId}", userId, Map.of("requestId", requestId));
    }
}
