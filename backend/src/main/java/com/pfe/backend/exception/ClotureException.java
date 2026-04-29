package com.pfe.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée quand une opération est bloquée par la clôture journalière — HTTP 409.
 *
 * <p>Exemples : tentative de saisie d'écriture sur une journée clôturée,
 * ou déclenchement d'une clôture alors qu'une opération est en cours.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ClotureException extends RuntimeException {

    public ClotureException(String message) {
        super(message);
    }
}
