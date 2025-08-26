package com.algebra.algebra_solver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EquationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(EquationNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidEquationException.class)
    public ResponseEntity<Map<String, Object>> badRequest(InvalidEquationException ex) {
        String msg = ex.getMessage();
        if (msg != null && msg.contains("No real roots")) {
            // Treat unsolvable math equations separately
            return error(HttpStatus.UNPROCESSABLE_ENTITY, "Math error: " + msg);
        }
        return error(HttpStatus.BAD_REQUEST, "Invalid equation: " + msg);
    }

    @ExceptionHandler({ IllegalArgumentException.class })
    public ResponseEntity<Map<String, Object>> badInput(IllegalArgumentException ex) {
        // Keep "Invalid input" consistent with tests too
        return error(HttpStatus.BAD_REQUEST, "Invalid input: " + ex.getMessage());
    }

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<Map<String, Object>> mathError(ArithmeticException ex) {
        // 422 is more correct for unsolvable / math errors
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "Math error: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unknown(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", status.getReasonPhrase());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}