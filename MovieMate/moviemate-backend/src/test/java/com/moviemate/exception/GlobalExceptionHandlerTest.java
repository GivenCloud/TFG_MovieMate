package com.moviemate.exception;

import com.moviemate.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleAuthenticationException_shouldReturnUnauthorized() {
        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(new BadCredentialsException("bad"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError()).isEqualTo("AUTHENTICATION_ERROR");
    }

    @Test
    void handleUserAlreadyExists_shouldReturnConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExists(new UserAlreadyExistsException("exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("USER_ALREADY_EXISTS");
    }

    @Test
    void handleValidationExceptions_shouldReturnDetails() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "username", "requerido"));
        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class),
                0
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getDetails()).containsEntry("username", "requerido");
    }

    @Test
    void handleNullPointerException_shouldReturnInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleNullPointerException(new NullPointerException("npe"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void handleRuntimeException_shouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("RUNTIME_ERROR");
    }

    @Test
    void handleTypeMismatchException_shouldReturnBadRequest() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc",
                Integer.class,
                "userId",
                null,
                new IllegalArgumentException("bad")
        );

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatchException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("TYPE_MISMATCH_ERROR");
    }

    @Test
    void handleDuplicateListName_shouldReturnConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateListName(new DuplicateListNameException("Fav"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("LIST_NAME_ALREADY_EXISTS");
    }

    @Test
    void handlePrivateProfileAndList_shouldReturnForbidden() {
        ResponseEntity<ErrorResponse> profileResponse = handler.handlePrivateProfile(new ProfilePrivateException("private"));
        ResponseEntity<ErrorResponse> listResponse = handler.handlePrivateList(new ListPrivateException("private list"));

        assertThat(profileResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(profileResponse.getBody().getError()).isEqualTo("PRIVATE_PROFILE");
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(listResponse.getBody().getError()).isEqualTo("PRIVATE_LIST");
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }
}
