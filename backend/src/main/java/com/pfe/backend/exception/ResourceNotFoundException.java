package com.pfe.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée quand une ressource demandée est introuvable — HTTP 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(resourceType + " introuvable : " + resourceId);
        this.resourceType = resourceType;
        this.resourceId   = resourceId;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.resourceId   = null;
    }

    public String getResourceType() { return resourceType; }
    public Object getResourceId()   { return resourceId; }
}
