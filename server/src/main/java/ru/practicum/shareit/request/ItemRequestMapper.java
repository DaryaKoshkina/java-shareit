package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;

public class ItemRequestMapper {

    private ItemRequestMapper() {
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemShortDto> items) {
        if (itemRequest == null) {
            return null;
        }
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                items == null ? Collections.emptyList() : items
        );
    }

    public static ItemShortDto toItemShortDto(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemShortDto(
                item.getId(),
                item.getName(),
                item.getOwner() != null ? item.getOwner().getId() : null
        );
    }
}
