package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeCreatedInIsoFormat() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(
                1L,
                "Нужна дрель",
                LocalDateTime.of(2026, 11, 1, 10, 30),
                List.of(new ItemShortDto(2L, "Дрель", 3L))
        );

        assertThat(json.write(dto)).hasJsonPathNumberValue("$.id");
        assertThat(json.write(dto)).hasJsonPathStringValue("$.description");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-11-01T10:30:00");
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.items[0].ownerId")
                .isEqualTo(3);
    }

    @Test
    void shouldDeserializeCreatedAndAnswers() throws Exception {
        ItemRequestDto dto = json.parseObject("{\"id\":1,\"description\":\"Нужна дрель\"," +
                "\"created\":\"2026-11-01T10:30:00\",\"items\":[{\"id\":2,\"name\":\"Дрель\",\"ownerId\":3}]}");

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 11, 1, 10, 30));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(3L);
    }
}
