package com.pfe.backend.wallet;

import com.pfe.backend.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * Filtre Spring qui protège {@code POST /api/v1/wallet/callback} contre les
 * appels non signés.
 *
 * <p>Seules les requêtes portant un header {@code X-Bankily-Signature} valide
 * (HMAC-SHA256 du body avec le secret partagé) sont transmises au contrôleur.
 * Toute autre requête reçoit une réponse {@code 401 Unauthorized}.</p>
 *
 * <p>Le filtre utilise {@link ContentCachingRequestWrapper} pour pouvoir lire
 * le corps de la requête deux fois (une fois pour la vérification, une fois
 * pour la désérialisation par Spring MVC).</p>
 */
@Component
public class BankilySignatureFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BankilySignatureFilter.class);
    private static final String CALLBACK_PATH = "/api/v1/wallet/callback";
    private static final String SIGNATURE_HEADER = "X-Bankily-Signature";

    private final WalletSignatureVerifier verifier;

    public BankilySignatureFilter(WalletSignatureVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // N'intercepte que POST /api/v1/wallet/callback
        return !("POST".equalsIgnoreCase(request.getMethod())
                 && CALLBACK_PATH.equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        // Enveloppe pour permettre la double lecture du body
        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);

        // Force la lecture complète du body afin que ContentCachingRequestWrapper
        // en garde une copie
        wrapper.getInputStream().readAllBytes();

        String signature = wrapper.getHeader(SIGNATURE_HEADER);
        byte[] body      = wrapper.getContentAsByteArray();

        try {
            verifier.verify(signature, body);
        } catch (BusinessException ex) {
            log.warn("[BankilySignatureFilter] Rejet callback : {}", ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\":\"" + ex.getMessage().replace("\"", "'") + "\"}");
            return;
        }

        chain.doFilter(wrapper, response);
    }
}
