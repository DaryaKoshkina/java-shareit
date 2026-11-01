package ru.practicum.shareit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Component
public class ItemRequestClient extends BaseClient {

    public ItemRequestClient(@Value("${shareit-server.url}") String baseUrl, ObjectMapper objectMapper) {
        super(baseUrl, objectMapper);
    }

    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        return post("/requests", userId, itemRequestDto, ItemRequestDto.class);
    }

    public List<ItemRequestDto> getOwn(Long userId) {
        return get("/requests", userId, new ParameterizedTypeReference<List<ItemRequestDto>>() {
        });
    }

    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        return get("/requests/all?from={from}&size={size}", userId,
                new ParameterizedTypeReference<List<ItemRequestDto>>() {
                }, from, size);
    }

    public ItemRequestDto getById(Long userId, Long requestId) {
        return get("/requests/" + requestId, userId, ItemRequestDto.class);
    }
}
