package ru.netology.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.netology.model.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.model.WrongInputDataException;

@RestControllerAdvice
public class ExceptionsHandler {
    @ExceptionHandler(WrongInputDataException.class)
    public ResponseEntity<ErrorResponse> handle(WrongInputDataException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(ex.getDescription(), ex.getId()),
                HttpStatus.BAD_REQUEST
        );
    }
}
