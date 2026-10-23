package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Collection;

public interface BookingService {
    BookingDto create(Long userId, BookItemRequestDto requestDto);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    Collection<BookingDto> getByBooker(Long userId, BookingState state);

    Collection<BookingDto> getByOwner(Long userId, BookingState state);
}
