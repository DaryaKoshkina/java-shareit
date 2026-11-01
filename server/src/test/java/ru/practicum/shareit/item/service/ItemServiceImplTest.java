package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void shouldCreateItemForOwner() {
        User owner = createUser("Иван", "ivan@example.com");

        ItemDto created = itemService.create(owner.getId(), itemDto("Дрель", "Аккумуляторная", true));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Дрель");
        assertThat(created.getRequestId()).isNull();
    }

    @Test
    void shouldThrowWhenCreatingItemForUnknownUser() {
        assertThatThrownBy(() -> itemService.create(999L, itemDto("Дрель", "Аккумуляторная", true)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnOwnerItemsWithBookingsAndComments() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, "Дрель", true);
        LocalDateTime now = LocalDateTime.now();
        Booking last = createBooking(item, booker, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);
        Booking next = createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        commentRepository.save(new Comment(null, "Хорошая дрель", item, booker, now.minusHours(1)));

        Collection<ItemDto> items = itemService.getAll(owner.getId());

        assertThat(items).hasSize(1);
        ItemDto result = items.iterator().next();
        assertThat(result.getLastBooking().getId()).isEqualTo(last.getId());
        assertThat(result.getNextBooking().getId()).isEqualTo(next.getId());
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getText()).isEqualTo("Хорошая дрель");
        assertThat(result.getComments().get(0).getAuthorName()).isEqualTo("Пётр");
    }

    @Test
    void shouldThrowWhenGettingItemsOfUnknownUser() {
        assertThatThrownBy(() -> itemService.getAll(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnItemWithoutBookingsForOtherUser() {
        User owner = createUser("Иван", "ivan@example.com");
        User other = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, "Дрель", true);

        ItemDto found = itemService.getById(other.getId(), item.getId());

        assertThat(found.getName()).isEqualTo("Дрель");
        assertThat(found.getLastBooking()).isNull();
        assertThat(found.getNextBooking()).isNull();
        assertThat(found.getComments()).isEmpty();
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        User owner = createUser("Иван", "ivan@example.com");

        assertThatThrownBy(() -> itemService.getById(owner.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateItemFields() {
        User owner = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, "Дрель", true);
        ItemDto update = new ItemDto();
        update.setName("Перфоратор");
        update.setDescription("Мощный");
        update.setAvailable(false);

        ItemDto updated = itemService.update(owner.getId(), item.getId(), update);

        assertThat(updated.getName()).isEqualTo("Перфоратор");
        assertThat(updated.getDescription()).isEqualTo("Мощный");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void shouldKeepItemFieldsWhenUpdateValuesAreBlank() {
        User owner = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, "Дрель", true);
        ItemDto update = new ItemDto();
        update.setName("   ");
        update.setDescription("   ");

        ItemDto updated = itemService.update(owner.getId(), item.getId(), update);

        assertThat(updated.getName()).isEqualTo("Дрель");
        assertThat(updated.getDescription()).isEqualTo("Описание Дрель");
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void shouldThrowWhenUpdatingItemByNotOwner() {
        User owner = createUser("Иван", "ivan@example.com");
        User other = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, "Дрель", true);

        assertThatThrownBy(() -> itemService.update(other.getId(), item.getId(), itemDto("Перфоратор", "Мощный", true)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenUpdatingUnknownItem() {
        User owner = createUser("Иван", "ivan@example.com");

        assertThatThrownBy(() -> itemService.update(owner.getId(), 999L, itemDto("Дрель", "Аккумуляторная", true)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindAvailableItemsByText() {
        User owner = createUser("Иван", "ivan@example.com");
        createItem(owner, "Дрель", true);
        createItem(owner, "Лопата", true);

        Collection<ItemDto> found = itemService.search("дрель");

        assertThat(found).extracting(ItemDto::getName).containsExactly("Дрель");
    }

    @Test
    void shouldReturnEmptyListForBlankSearch() {
        assertThat(itemService.search("   ")).isEmpty();
        assertThat(itemService.search(null)).isEmpty();
    }

    @Test
    void shouldAddCommentAfterCompletedBooking() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, "Дрель", true);
        LocalDateTime now = LocalDateTime.now();
        createBooking(item, booker, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель");

        CommentDto created = itemService.addComment(booker.getId(), item.getId(), commentDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getText()).isEqualTo("Отличная дрель");
        assertThat(created.getAuthorName()).isEqualTo("Пётр");
        assertThat(created.getCreated()).isNotNull();
    }

    @Test
    void shouldRejectCommentWithoutCompletedBooking() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, "Дрель", true);
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(), commentDto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowWhenCommentAuthorNotFound() {
        User owner = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, "Дрель", true);
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель");

        assertThatThrownBy(() -> itemService.addComment(999L, item.getId(), commentDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenCommentingUnknownItem() {
        User booker = createUser("Пётр", "petr@example.com");
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), 999L, commentDto))
                .isInstanceOf(NotFoundException.class);
    }

    private User createUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private Item createItem(User owner, String name, boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("Описание " + name);
        item.setAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end,
                                  BookingStatus status) {
        return bookingRepository.save(new Booking(null, start, end, item, booker, status));
    }

    private ItemDto itemDto(String name, String description, boolean available) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        return itemDto;
    }
}
