package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Collection<UserDto> getAll() {
        return userRepository.getAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.getById(id);
        if (user == null) throw new NotFoundException("Пользователь с id " + id + " не найден");
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto create(UserDto userDto) {
        User existingWithEmail = userRepository.getByEmail(userDto.getEmail());
        if (existingWithEmail != null) {
            throw new ConflictException("Email " + userDto.getEmail() + " уже занят");
        }

        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.create(user));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existing = userRepository.getById(id);
        if (existing == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            User existingWithEmail = userRepository.getByEmail(userDto.getEmail());
            if (existingWithEmail != null && !existingWithEmail.getId().equals(id)) {
                throw new ConflictException("Email " + userDto.getEmail() + " уже занят другим пользователем");
            }
            existing.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existing.setName(userDto.getName());
        }

        return UserMapper.toUserDto(userRepository.update(existing));
    }

    @Override
    public void delete(Long id) {
        if (userRepository.getById(id) == null) throw new NotFoundException("Пользователь с id " + id + " не найден");
        userRepository.delete(id);
    }
}