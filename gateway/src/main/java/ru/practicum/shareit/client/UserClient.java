package ru.practicum.shareit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Component
public class UserClient extends BaseClient {

    public UserClient(@Value("${shareit-server.url}") String baseUrl, ObjectMapper objectMapper) {
        super(baseUrl, objectMapper);
    }

    public List<UserDto> getAll() {
        return get("/users", null, new ParameterizedTypeReference<List<UserDto>>() {
        });
    }

    public UserDto getById(Long userId) {
        return get("/users/" + userId, null, UserDto.class);
    }

    public UserDto create(UserDto userDto) {
        return post("/users", null, userDto, UserDto.class);
    }

    public UserDto update(Long userId, UserDto userDto) {
        return patch("/users/" + userId, null, userDto, UserDto.class);
    }

    public void delete(Long userId) {
        delete("/users/" + userId);
    }
}
