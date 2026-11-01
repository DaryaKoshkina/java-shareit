package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void shouldCreateRequestWithCreatedDateAndEmptyAnswers() {
        User requestor = createUser("Иван", "ivan@example.com");

        ItemRequestDto created = itemRequestService.create(requestor.getId(),
                new ItemRequestDto(null, "Нужна дрель", null, null));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Нужна дрель");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void shouldReturnOwnRequestsNewestFirst() {
        User requestor = createUser("Пётр", "petr@example.com");
        saveRequest(requestor, "Старый запрос", LocalDateTime.now().minusDays(2));
        saveRequest(requestor, "Новый запрос", LocalDateTime.now().minusDays(1));

        List<ItemRequestDto> requests = itemRequestService.getOwn(requestor.getId());

        assertThat(requests)
                .extracting(ItemRequestDto::getDescription)
                .containsExactly("Новый запрос", "Старый запрос");
    }

    @Test
    void shouldReturnOnlyOtherUsersRequests() {
        User requestor = createUser("Анна", "anna@example.com");
        User other = createUser("Олег", "oleg@example.com");
        saveRequest(requestor, "Запрос Анны", LocalDateTime.now());
        saveRequest(other, "Запрос Олега", LocalDateTime.now().plusMinutes(1));

        List<ItemRequestDto> requests = itemRequestService.getAll(requestor.getId(), 0, 20);

        assertThat(requests)
                .extracting(ItemRequestDto::getDescription)
                .containsExactly("Запрос Олега");
    }

    @Test
    void shouldReturnRequestWithItemAnswers() {
        User requestor = createUser("Мария", "maria@example.com");
        User owner = createUser("Сергей", "sergey@example.com");
        ItemRequest request = saveRequest(requestor, "Нужна дрель", LocalDateTime.now());

        ItemDto item = new ItemDto("Дрель", "Аккумуляторная", true, request.getId());
        ItemDto createdItem = itemService.create(owner.getId(), item);

        ItemRequestDto found = itemRequestService.getById(owner.getId(), request.getId());

        assertThat(found.getItems()).hasSize(1);
        ItemShortDto answer = found.getItems().get(0);
        assertThat(answer.getId()).isEqualTo(createdItem.getId());
        assertThat(answer.getName()).isEqualTo("Дрель");
        assertThat(answer.getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void shouldRejectItemForUnknownRequest() {
        User owner = createUser("Никита", "nikita@example.com");
        ItemDto item = new ItemDto("Дрель", "Аккумуляторная", true, 999L);

        assertThatThrownBy(() -> itemService.create(owner.getId(), item))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenRequestNotFound() {
        User user = createUser("Ольга", "olga@example.com");

        assertThatThrownBy(() -> itemRequestService.getById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getOwn(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private User createUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private ItemRequest saveRequest(User requestor, String description, LocalDateTime created) {
        return itemRequestRepository.save(new ItemRequest(null, description, requestor, created));
    }
}
