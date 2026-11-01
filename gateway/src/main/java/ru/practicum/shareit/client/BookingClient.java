package ru.practicum.shareit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@Component
public class BookingClient extends BaseClient {

    public BookingClient(@Value("${shareit-server.url}") String baseUrl, ObjectMapper objectMapper) {
        super(baseUrl, objectMapper);
    }

    public BookingDto create(Long userId, BookItemRequestDto requestDto) {
        return post("/bookings", userId, requestDto, BookingDto.class);
    }

    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        return patch("/bookings/" + bookingId + "?approved=" + approved, userId, BookingDto.class);
    }

    public BookingDto getById(Long userId, Long bookingId) {
        return get("/bookings/" + bookingId, userId, BookingDto.class);
    }

    public List<BookingDto> getByBooker(Long userId, String state) {
        return get("/bookings?state={state}", userId, new ParameterizedTypeReference<List<BookingDto>>() {
        }, state);
    }

    public List<BookingDto> getByOwner(Long userId, String state) {
        return get("/bookings/owner?state={state}", userId, new ParameterizedTypeReference<List<BookingDto>>() {
        }, state);
    }
}
