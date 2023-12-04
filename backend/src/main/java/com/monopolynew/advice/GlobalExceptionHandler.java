package com.monopolynew.advice;

import com.monopolynew.dto.ErrorDTO;
import com.monopolynew.exception.BadRequestException;
import com.monopolynew.exception.ExceptionCode;
import com.monopolynew.exception.WrongGameStageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String UNEXPECTED = "Unexpected server error: ";

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO resolveBadRequest(BadRequestException exception) {
        return ErrorDTO.builder()
                .message(exception.getMessage())
                .code(exception.getCode())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO resolveBadRequest(HttpMessageNotReadableException exception) {
        return ErrorDTO.builder()
                .message(exception.getMessage())
                .code(ExceptionCode.CLIENT_REQUEST.getCode())
                .build();
    }

    @ExceptionHandler(WrongGameStageException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDTO resolveWrongGameStage(WrongGameStageException exception) {
        return ErrorDTO.builder()
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDTO resolveServerError(Exception exception) {
        log.error(UNEXPECTED, exception);
        return ErrorDTO.builder()
                .message(UNEXPECTED + exception.getMessage())
                .build();
    }
}