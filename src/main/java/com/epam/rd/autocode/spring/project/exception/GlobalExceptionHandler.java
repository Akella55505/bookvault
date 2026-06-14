package com.epam.rd.autocode.spring.project.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex) {
        return handleException(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return handleException(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ModelAndView handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return handleException(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Suspicious activity detected. Possible page scraping");
        return handleException(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFoundException(NotFoundException ex) {
        return handleException(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("Suspicious activity detected. Possible http method enumeration");
        return handleException(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAll(Exception ex) {
        return handleException(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private ModelAndView handleException(HttpStatus status, Exception ex) {
        log.error(ex.getMessage());

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("requestUri", "/error");
        mav.addObject("status", status.value());

        return mav;
    }
}
