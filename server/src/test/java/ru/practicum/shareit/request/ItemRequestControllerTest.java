package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemRequestService itemRequestService;

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestDto response = new ItemRequestDto(1L, "Нужна дрель",
                LocalDateTime.of(2026, 11, 1, 10, 0), List.of());
        when(itemRequestService.create(eq(5L), any(ItemRequestDto.class))).thenReturn(response);

        ItemRequestDto body = new ItemRequestDto(null, "Нужна дрель", null, null);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void shouldReturnOwnRequests() throws Exception {
        when(itemRequestService.getOwn(5L)).thenReturn(List.of(
                new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of())));

        mockMvc.perform(get("/requests").header(USER_ID_HEADER, 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void shouldPassPaginationToService() throws Exception {
        when(itemRequestService.getAll(5L, 2, 5)).thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 5L)
                        .param("from", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(itemRequestService).getAll(5L, 2, 5);
    }

    @Test
    void shouldReturnRequestWithAnswers() throws Exception {
        ItemRequestDto response = new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(),
                List.of(new ItemShortDto(10L, "Дрель", 3L)));
        when(itemRequestService.getById(5L, 1L)).thenReturn(response);

        mockMvc.perform(get("/requests/1").header(USER_ID_HEADER, 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(10))
                .andExpect(jsonPath("$.items[0].name").value("Дрель"))
                .andExpect(jsonPath("$.items[0].ownerId").value(3));
    }

    @Test
    void shouldReturnNotFoundForUnknownRequest() throws Exception {
        when(itemRequestService.getById(5L, 99L))
                .thenThrow(new NotFoundException("Запрос с id 99 не найден"));

        mockMvc.perform(get("/requests/99").header(USER_ID_HEADER, 5L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWithoutUserHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNonNumericRequestId() throws Exception {
        mockMvc.perform(get("/requests/not-a-number").header(USER_ID_HEADER, 5L))
                .andExpect(status().isBadRequest());
    }
}
