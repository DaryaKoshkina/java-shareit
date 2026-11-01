package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    @Test
    void shouldReturnNullForNullArguments() {
        assertThat(UserMapper.toUserDto(null)).isNull();
        assertThat(UserMapper.toUser(null)).isNull();
        assertThat(ItemMapper.toItemDto(null)).isNull();
        assertThat(ItemMapper.toItem(null)).isNull();
        assertThat(CommentMapper.toCommentDto(null)).isNull();
        assertThat(BookingMapper.toBookingDto(null)).isNull();
    }

    @Test
    void shouldMapUserInBothDirections() {
        User user = UserMapper.toUser(dtoWithEmail("ivan@example.com"));
        user.setId(1L);

        assertThat(UserMapper.toUserDto(user).getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void shouldMapItemWithoutAvailableFlagAndRequest() {
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");

        Item item = ItemMapper.toItem(dto);

        assertThat(item.isAvailable()).isFalse();
        assertThat(item.getRequest()).isNull();
    }

    @Test
    void shouldReturnRequestIdWhenItemHasRequest() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);
        ItemRequest request = new ItemRequest();
        request.setId(5L);
        item.setRequest(request);

        assertThat(ItemMapper.toItemDto(item).getRequestId()).isEqualTo(5L);
    }

    @Test
    void shouldMapCommentWithUnknownAuthor() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Отзыв");
        comment.setCreated(LocalDateTime.now());

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertThat(dto.getText()).isEqualTo("Отзыв");
        assertThat(dto.getAuthorName()).isNull();
    }

    @Test
    void shouldMapBooking() {
        User owner = new User(1L, "Иван", "ivan@example.com");
        User booker = new User(2L, "Пётр", "petr@example.com");
        Item item = new Item(5L, "Дрель", "Аккумуляторная", true, owner, null);
        Booking booking = new Booking(7L, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                item, booker, BookingStatus.APPROVED);

        BookingDto dto = BookingMapper.toBookingDto(booking);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getItem().getId()).isEqualTo(5L);
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    private UserDto dtoWithEmail(String email) {
        return new UserDto(null, "Иван", email);
    }
}
