package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

@Component
public class UserClient extends BaseClient {

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.rootUri(serverUrl).build());
    }

    public ResponseEntity<Object> getAll() {
        return get("/users");
    }

    public ResponseEntity<Object> getById(Long userId) {
        return get("/users/{id}", null, Map.of("id", userId));
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        return post("/users", userDto);
    }

    public ResponseEntity<Object> update(Long userId, UserDto userDto) {
        return patch("/users/{id}", null, Map.of("id", userId), userDto);
    }

    public ResponseEntity<Object> delete(Long userId) {
        return delete("/users/{id}", null, Map.of("id", userId));
    }
}
