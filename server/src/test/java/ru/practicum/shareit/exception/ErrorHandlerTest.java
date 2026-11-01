package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErrorHandlerTest {
    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void shouldHandleNotFound() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleNotFound(new NotFoundException("не найдено"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isEqualTo("не найдено");
    }

    @Test
    void shouldHandleConflict() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleConflict(new ConflictException("конфликт"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("конфликт");
    }

    @Test
    void shouldHandleForbidden() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleForbidden(new ForbiddenException("запрещено"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError()).isEqualTo("запрещено");
    }

    @Test
    void shouldHandleValidation() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleValidation(mock(MethodArgumentNotValidException.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Ошибка валидации данных");
    }

    @Test
    void shouldHandleMissingHeader() {
        MissingRequestHeaderException exception = mock(MissingRequestHeaderException.class);
        when(exception.getHeaderName()).thenReturn("X-Sharer-User-Id");

        ResponseEntity<ErrorResponse> response = errorHandler.handleMissingHeader(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError())
                .isEqualTo("Отсутствует обязательный заголовок X-Sharer-User-Id");
    }

    @Test
    void shouldHandleTypeMismatch() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("itemId");

        ResponseEntity<ErrorResponse> response = errorHandler.handleTypeMismatch(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Некорректное значение параметра itemId");
    }

    @Test
    void shouldHandleNotReadableBody() {
        ResponseEntity<ErrorResponse> response =
                errorHandler.handleNotReadable(mock(HttpMessageNotReadableException.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Некорректное тело запроса");
    }

    @Test
    void shouldHandleBadRequest() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleBadRequest(new BadRequestException("плохой запрос"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("плохой запрос");
    }

    @Test
    void shouldHandleUnexpectedError() {
        ResponseEntity<ErrorResponse> response = errorHandler.handleThrowable(new IllegalStateException("сбой"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isEqualTo("Произошла непредвиденная ошибка.");
    }
}
