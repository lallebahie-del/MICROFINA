package com.pfe.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception métier générique — HTTP 422 Unprocessable Entity.
 *
 * <p>Levée quand la requête est syntaxiquement correcte mais enfreint
 * une règle métier (ex : transition de statut interdite, solde insuffisant).</p>
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
