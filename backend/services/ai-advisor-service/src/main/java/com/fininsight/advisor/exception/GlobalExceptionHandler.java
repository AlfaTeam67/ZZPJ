package com.fininsight.advisor.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecommendationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(RecommendationNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recommendation not found", ex.getMessage(), "recommendation-not-found");
    }

    @ExceptionHandler(PortfolioNotAvailableException.class)
    public ResponseEntity<ProblemDetail> handlePortfolio(PortfolioNotAvailableException ex) {
        return problem(HttpStatus.BAD_GATEWAY, "Portfolio service unavailable", ex.getMessage(), "portfolio-unavailable");
    }

    @ExceptionHandler(LlmUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleLlm(LlmUnavailableException ex) {
        log.warn("LLM unavailable: {}", ex.getMessage());
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Recommendation engine unavailable", ex.getMessage(), "llm-unavailable");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
            errors.put(fe.getField(), fe.getDefaultMessage()));
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        body.setTitle("Validation failed");
        body.setType(URI.create("https://fin-insight.dev/problems/validation"));
        body.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException ex) {
        return problem(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(),
            ex.getReason(), "response-status");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail, String type) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        body.setType(URI.create("https://fin-insight.dev/problems/" + type));
        return ResponseEntity.status(status).body(body);
    }
}
