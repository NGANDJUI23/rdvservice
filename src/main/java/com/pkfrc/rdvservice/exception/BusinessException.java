package com.pkfrc.rdvservice.exception;


import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final String message;


    public BusinessException(String format) {
        super(format);
        this.code = "";
        this.message = format;
    }
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}
