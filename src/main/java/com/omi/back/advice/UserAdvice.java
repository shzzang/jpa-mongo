package com.omi.back.advice;

import com.omi.back.domain.ErrorEntity;
import com.omi.back.exception.UserEmailExistException;
import com.omi.back.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class UserAdvice {

    @ResponseBody
    @ExceptionHandler(UserEmailExistException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorEntity emailExistHandler(UserEmailExistException ex) {
        return new ErrorEntity(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorEntity notFoundHandler(UserNotFoundException ex) {
        return new ErrorEntity(ex.getMessage());
    }

}
