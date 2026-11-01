package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

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

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @Test
    void shouldReturnOwnerItems() throws Exception {
        when(itemService.getAll(1L)).thenReturn(List.of(itemDto(1L, "Дрель")));

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void shouldReturnBadRequestWithoutUserHeader() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Отсутствует обязательный заголовок X-Sharer-User-Id"));
    }

    @Test
    void shouldReturnItemById() throws Exception {
        when(itemService.getById(1L, 1L)).thenReturn(itemDto(1L, "Дрель"));

        mockMvc.perform(get("/items/1").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnNotFoundForUnknownItem() throws Exception {
        when(itemService.getById(1L, 99L)).thenThrow(new NotFoundException("Вещь с id 99 не найдена"));

        mockMvc.perform(get("/items/99").header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(itemDto(1L, "Перфоратор"));

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto(null, "Перфоратор"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Перфоратор"));
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(itemDto(1L, "Дрель")));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(5L);
        commentDto.setText("Отличная дрель");
        commentDto.setAuthorName("Пётр");
        commentDto.setCreated(LocalDateTime.now());
        when(itemService.addComment(eq(2L), eq(1L), any(CommentDto.class))).thenReturn(commentDto);

        CommentDto request = new CommentDto();
        request.setText("Отличная дрель");

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличная дрель"));

        verify(itemService).addComment(eq(2L), eq(1L), any(CommentDto.class));
    }

    private ItemDto itemDto(Long id, String name) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(id);
        itemDto.setName(name);
        itemDto.setDescription("Описание");
        itemDto.setAvailable(true);
        return itemDto;
    }
}
