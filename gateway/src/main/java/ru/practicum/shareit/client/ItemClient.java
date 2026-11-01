package ru.practicum.shareit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Component
public class ItemClient extends BaseClient {

    public ItemClient(@Value("${shareit-server.url}") String baseUrl, ObjectMapper objectMapper) {
        super(baseUrl, objectMapper);
    }

    public List<ItemDto> getAll(Long userId) {
        return get("/items", userId, new ParameterizedTypeReference<List<ItemDto>>() {
        });
    }

    public ItemDto getById(Long userId, Long itemId) {
        return get("/items/" + itemId, userId, ItemDto.class);
    }

    public ItemDto create(Long userId, ItemDto itemDto) {
        return post("/items", userId, itemDto, ItemDto.class);
    }

    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        return patch("/items/" + itemId, userId, itemDto, ItemDto.class);
    }

    public List<ItemDto> search(String text) {
        return get("/items/search?text={text}", null,
                new ParameterizedTypeReference<List<ItemDto>>() {
                }, text);
    }

    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        return post("/items/" + itemId + "/comment", userId, commentDto, CommentDto.class);
    }
}
