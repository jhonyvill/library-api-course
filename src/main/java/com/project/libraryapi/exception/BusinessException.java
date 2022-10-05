package com.project.libraryapi.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String messageError) {
        super(messageError);
    }
}
