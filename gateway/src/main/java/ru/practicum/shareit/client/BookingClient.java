package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.util.Map;

@Component
public class BookingClient extends BaseClient {

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.rootUri(serverUrl).build());
    }

    public ResponseEntity<Object> create(Long userId, BookItemRequestDto requestDto) {
        return post("/bookings", userId, requestDto);
    }

    public ResponseEntity<Object> approve(Long userId, Long bookingId, Boolean approved) {
        return patch("/bookings/{bookingId}?approved={approved}", userId,
                Map.of("bookingId", bookingId, "approved", approved), null);
    }

    public ResponseEntity<Object> getById(Long userId, Long bookingId) {
        return get("/bookings/{bookingId}", userId, Map.of("bookingId", bookingId));
    }

    public ResponseEntity<Object> getByBooker(Long userId, String state) {
        return get("/bookings?state={state}", userId, Map.of("state", state));
    }

    public ResponseEntity<Object> getByOwner(Long userId, String state) {
        return get("/bookings/owner?state={state}", userId, Map.of("state", state));
    }
}
