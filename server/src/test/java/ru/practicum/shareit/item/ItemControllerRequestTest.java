package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerRequestTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Test
    void shouldPassRequestIdToService() throws Exception {
        ItemDto response = new ItemDto(10L, "Дрель", "Аккумуляторная", true, 3L, null, null, null);
        when(itemService.create(eq(7L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Аккумуляторная\"," +
                                "\"available\":true,\"requestId\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(3));

        ArgumentCaptor<ItemDto> captor = ArgumentCaptor.forClass(ItemDto.class);
        verify(itemService).create(eq(7L), captor.capture());
        assertThat(captor.getValue().getRequestId()).isEqualTo(3L);
    }

    @Test
    void shouldCreateItemWithoutRequestId() throws Exception {
        ItemDto response = new ItemDto(10L, "Дрель", "Аккумуляторная", true, null, null, null, null);
        when(itemService.create(eq(7L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Аккумуляторная\"," +
                                "\"available\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").doesNotExist());

        ArgumentCaptor<ItemDto> captor = ArgumentCaptor.forClass(ItemDto.class);
        verify(itemService).create(eq(7L), captor.capture());
        assertThat(captor.getValue().getRequestId()).isNull();
    }
}
