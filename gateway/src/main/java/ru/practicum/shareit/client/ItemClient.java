package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Component
public class ItemClient extends BaseClient {

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.rootUri(serverUrl).build());
    }

    public ResponseEntity<Object> getAll(Long userId) {
        return get("/items", userId, null);
    }

    public ResponseEntity<Object> getById(Long userId, Long itemId) {
        return get("/items/{itemId}", userId, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> create(Long userId, ItemDto itemDto) {
        return post("/items", userId, itemDto);
    }

    public ResponseEntity<Object> update(Long userId, Long itemId, ItemDto itemDto) {
        return patch("/items/{itemId}", userId, Map.of("itemId", itemId), itemDto);
    }

    public ResponseEntity<Object> search(String text) {
        return get("/items/search?text={text}", null, Map.of("text", text));
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentDto commentDto) {
        return post("/items/{itemId}/comment", userId, Map.of("itemId", itemId), commentDto);
    }
}
