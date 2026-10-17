package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Collection<Item> getAll(Long ownerId);

    Item getById(Long id);

    Item create(Item item);

    Item update(Item item);

    Collection<Item> search(String text);
}