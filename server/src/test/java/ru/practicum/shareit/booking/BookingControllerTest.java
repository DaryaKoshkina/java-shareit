package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.create(eq(2L), any(BookItemRequestDto.class))).thenReturn(bookingDto(1L));

        BookItemRequestDto request = new BookItemRequestDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingService.approve(1L, 1L, true)).thenReturn(bookingDto(1L));

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnBookingById() throws Exception {
        when(bookingService.getById(1L, 1L)).thenReturn(bookingDto(1L));

        mockMvc.perform(get("/bookings/1").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnForbiddenForForeignBooking() throws Exception {
        when(bookingService.getById(1L, 1L)).thenThrow(new ForbiddenException("Бронирование недоступно"));

        mockMvc.perform(get("/bookings/1").header(USER_ID_HEADER, 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Бронирование недоступно"));
    }

    @Test
    void shouldReturnBookerBookingsWithDefaultState() throws Exception {
        when(bookingService.getByBooker(1L, BookingState.ALL)).thenReturn(List.of(bookingDto(1L)));

        mockMvc.perform(get("/bookings").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService).getByBooker(1L, BookingState.ALL);
    }

    @Test
    void shouldParseStateInAnyCase() throws Exception {
        when(bookingService.getByBooker(1L, BookingState.CURRENT)).thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "current"))
                .andExpect(status().isOk());

        verify(bookingService).getByBooker(1L, BookingState.CURRENT);
    }

    @Test
    void shouldReturnBadRequestForUnknownState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "WRONG"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: WRONG"));
    }

    @Test
    void shouldReturnOwnerBookings() throws Exception {
        when(bookingService.getByOwner(1L, BookingState.ALL)).thenReturn(List.of(bookingDto(1L)));

        mockMvc.perform(get("/bookings/owner").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    private BookingDto bookingDto(Long id) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        UserDto userDto = new UserDto(2L, "Пётр", "petr@example.com");

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(id);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItem(itemDto);
        bookingDto.setBooker(userDto);
        bookingDto.setStatus(BookingStatus.WAITING);
        return bookingDto;
    }
}
