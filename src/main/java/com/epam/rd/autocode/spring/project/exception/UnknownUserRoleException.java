package com.epam.rd.autocode.spring.project.exception;

public class UnknownUserRoleException extends RuntimeException {
    public UnknownUserRoleException(String message) {
        super(message);
    }
}
