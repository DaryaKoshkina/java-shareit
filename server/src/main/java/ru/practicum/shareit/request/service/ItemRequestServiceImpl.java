package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "created");

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest saved = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(saved, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        checkUserExists(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return enrich(requests);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        checkUserExists(userId);
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Параметры пагинации from и size заданы неверно");
        }
        Pageable pageable = PageRequest.of(from / size, size, NEWEST_FIRST);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNot(userId, pageable).getContent();
        return enrich(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        checkUserExists(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));
        return ItemRequestMapper.toItemRequestDto(itemRequest, findAnswers(List.of(itemRequest)).get(requestId));
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private List<ItemRequestDto> enrich(List<ItemRequest> requests) {
        Map<Long, List<ItemShortDto>> answers = findAnswers(requests);
        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(request, answers.get(request.getId())))
                .collect(Collectors.toList());
    }

    private Map<Long, List<ItemShortDto>> findAnswers(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        return itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemRequestMapper::toItemShortDto, Collectors.toList())
                ));
    }
}
