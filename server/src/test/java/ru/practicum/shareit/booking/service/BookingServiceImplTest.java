package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateBooking() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, true);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);

        BookingDto created = bookingService.create(booker.getId(), requestDto(item.getId(), start, end));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void shouldRejectBookingOfOwnItem() {
        User owner = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.create(owner.getId(),
                requestDto(item.getId(), start, start.plusDays(1))))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldRejectBookingOfUnavailableItem() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, false);
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.create(booker.getId(),
                requestDto(item.getId(), start, start.plusDays(1))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldRejectBookingWithoutDates() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, true);

        assertThatThrownBy(() -> bookingService.create(booker.getId(), requestDto(item.getId(), null, null)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldRejectBookingWithEndBeforeStart() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, true);
        LocalDateTime start = LocalDateTime.now().plusDays(2);

        assertThatThrownBy(() -> bookingService.create(booker.getId(),
                requestDto(item.getId(), start, start.minusDays(1))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowWhenBookingForUnknownUser() {
        User owner = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.create(999L, requestDto(item.getId(), start, start.plusDays(1))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenBookingUnknownItem() {
        User booker = createUser("Пётр", "petr@example.com");
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.create(booker.getId(), requestDto(999L, start, start.plusDays(1))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldApproveBooking() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Booking booking = createWaitingBooking(createItem(owner, true), booker);

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void shouldRejectBooking() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Booking booking = createWaitingBooking(createItem(owner, true), booker);

        BookingDto rejected = bookingService.approve(owner.getId(), booking.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void shouldRejectApproveByNotOwner() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Booking booking = createWaitingBooking(createItem(owner, true), booker);

        assertThatThrownBy(() -> bookingService.approve(booker.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldRejectSecondApprove() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Booking booking = createWaitingBooking(createItem(owner, true), booker);
        bookingService.approve(owner.getId(), booking.getId(), true);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), booking.getId(), true))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowWhenApprovingUnknownBooking() {
        User owner = createUser("Иван", "ivan@example.com");

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), 999L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnBookingForBookerAndOwner() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Booking booking = createWaitingBooking(createItem(owner, true), booker);

        assertThat(bookingService.getById(booker.getId(), booking.getId()).getId()).isEqualTo(booking.getId());
        assertThat(bookingService.getById(owner.getId(), booking.getId()).getId()).isEqualTo(booking.getId());
    }

    @Test
    void shouldForbidBookingForOtherUser() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        User other = createUser("Олег", "oleg@example.com");
        Booking booking = createWaitingBooking(createItem(owner, true), booker);

        assertThatThrownBy(() -> bookingService.getById(other.getId(), booking.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowWhenGettingUnknownBooking() {
        User user = createUser("Иван", "ivan@example.com");

        assertThatThrownBy(() -> bookingService.getById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFilterBookerBookingsByState() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, true);
        LocalDateTime now = LocalDateTime.now();
        createBooking(item, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);
        createBooking(item, booker, now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED);
        createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        createBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.REJECTED);

        assertThat(bookingService.getByBooker(booker.getId(), BookingState.ALL)).hasSize(4);
        assertThat(bookingService.getByBooker(booker.getId(), BookingState.CURRENT)).hasSize(1);
        assertThat(bookingService.getByBooker(booker.getId(), BookingState.PAST)).hasSize(1);
        assertThat(bookingService.getByBooker(booker.getId(), BookingState.FUTURE)).hasSize(2);
        assertThat(bookingService.getByBooker(booker.getId(), BookingState.WAITING)).hasSize(1);
        assertThat(bookingService.getByBooker(booker.getId(), BookingState.REJECTED)).hasSize(1);
    }

    @Test
    void shouldFilterOwnerBookingsByState() {
        User owner = createUser("Иван", "ivan@example.com");
        User booker = createUser("Пётр", "petr@example.com");
        Item item = createItem(owner, true);
        LocalDateTime now = LocalDateTime.now();
        createBooking(item, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);
        createBooking(item, booker, now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED);
        createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        createBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.REJECTED);

        assertThat(bookingService.getByOwner(owner.getId(), BookingState.ALL)).hasSize(4);
        assertThat(bookingService.getByOwner(owner.getId(), BookingState.CURRENT)).hasSize(1);
        assertThat(bookingService.getByOwner(owner.getId(), BookingState.PAST)).hasSize(1);
        assertThat(bookingService.getByOwner(owner.getId(), BookingState.FUTURE)).hasSize(2);
        assertThat(bookingService.getByOwner(owner.getId(), BookingState.WAITING)).hasSize(1);
        assertThat(bookingService.getByOwner(owner.getId(), BookingState.REJECTED)).hasSize(1);
    }

    @Test
    void shouldThrowWhenBookerNotFound() {
        assertThatThrownBy(() -> bookingService.getByBooker(999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenOwnerNotFound() {
        assertThatThrownBy(() -> bookingService.getByOwner(999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }

    private User createUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private Item createItem(User owner, boolean available) {
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end,
                                  BookingStatus status) {
        return bookingRepository.save(new Booking(null, start, end, item, booker, status));
    }

    private Booking createWaitingBooking(Item item, User booker) {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        return createBooking(item, booker, start, start.plusDays(1), BookingStatus.WAITING);
    }

    private BookItemRequestDto requestDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(start);
        requestDto.setEnd(end);
        return requestDto;
    }
}
