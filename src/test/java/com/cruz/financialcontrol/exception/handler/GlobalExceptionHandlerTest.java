package com.cruz.financialcontrol.exception.handler;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import com.cruz.financialcontrol.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_shouldCollectFieldErrorsIntoProblemDetail() {
        FieldError fieldError = new FieldError("createAccountDTO", "name", "Name is required");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<ProblemDetail> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Validation Failed");
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) response.getBody().getProperties().get("errors");
        assertThat(errors).containsExactly("name: Name is required");
    }

    @Test
    void handleBusinessRuleException_shouldReturnStatusFromException() {
        BusinessRuleException ex = new BusinessRuleException("Account with name 'Savings' already exists.");

        ResponseEntity<ProblemDetail> response = handler.handleBusinessRuleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Account with name 'Savings' already exists.");
        assertThat(response.getBody().getTitle()).isEqualTo("Business Rule Violation");
    }

    @Test
    void handleBusinessRuleException_shouldRespectCustomStatus() {
        BusinessRuleException ex = new BusinessRuleException("Conflict detail", HttpStatus.CONFLICT);

        ResponseEntity<ProblemDetail> response = handler.handleBusinessRuleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleNotFound_shouldAlwaysReturn404() {
        NotFoundException ex = new NotFoundException("Account not found with id: 99");

        ResponseEntity<ProblemDetail> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Account not found with id: 99");
    }

    @Test
    void handleNoResourceFound_shouldReturn404WithResourcePath() {
        NoResourceFoundException ex = new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/unknown/path", "/unknown/path");

        ResponseEntity<ProblemDetail> response = handler.handleNoResourceFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).contains("/unknown/path");
    }

    @Test
    void handleMethodNotSupported_shouldReturn405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ProblemDetail> response = handler.handleMethodNotSupported(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void handleMessageNotReadable_shouldReturn400WithGenericDetail() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("broken json", (org.springframework.http.HttpInputMessage) null);

        ResponseEntity<ProblemDetail> response = handler.handleMessageNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Malformed or missing request body");
    }

    @Test
    void handleBadCredentialsException_shouldReturn401() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ProblemDetail> response = handler.handleBadCredentialsException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Bad credentials");
    }

    @Test
    void handleGenericException_shouldReturn500WithoutLeakingInternalDetails() {
        RuntimeException ex = new RuntimeException("some internal secret stack trace detail");

        ResponseEntity<ProblemDetail> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail())
                .isEqualTo("An unexpected error occurred")
                .doesNotContain("internal secret stack trace detail");
    }
}
