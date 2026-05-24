package com.pkfrc.rdvservice.exception;


import lombok.Getter;

@Getter
public class ResourceNotFoundException extends BusinessException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s non trouvé avec %s : '%s'%n", resourceName, fieldName, fieldValue));
//        super(String.format("%s non trouvé avec %s : '%s'", resourceName, fieldName, fieldValue));
        System.out.println("je suis dans cette exception ");
        System.out.printf("%s non trouvé avec %s : '%s'%n", resourceName, fieldName, fieldValue);
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

}
