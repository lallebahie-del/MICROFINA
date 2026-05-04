package com.pfe.backend.wallet;

import com.pfe.backend.dto.WalletDto.BankilyInitResponse;
import com.pfe.backend.dto.WalletDto.BankilyStatutResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/**
 * BankilyClient — client HTTP pour l'API Bankily (monnaie mobile mauritanienne).
 *
 * <h2>Endpoints Bankily utilisés</h2>
 * <pre>
 *   POST /payment/initiate    — décaissement MFI → wallet client
 *   POST /collection/initiate — collecte wallet client → MFI
 *   GET  /transaction/{ref}   — consultation du statut d'une transaction
 * </pre>
 *
 * <h2>Codes retour Bankily</h2>
 * <ul>
 *   <li>{@code SUCCESS}            — transaction confirmée</li>
 *   <li>{@code PENDING}            — en attente de confirmation client</li>
 *   <li>{@code INSUFFICIENT_FUNDS} — solde insuffisant</li>
 *   <li>{@code INVALID_MSISDN}     — numéro de téléphone invalide ou non Bankily</li>
 *   <li>{@code TIMEOUT}            — délai d'attente dépassé côté opérateur</li>
 *   <li>{@code FAILED}             — échec générique</li>
 * </ul>
 *
 * <p>Toutes les erreurs HTTP (4xx/5xx) sont capturées et converties en
 * {@link BankilyInitResponse} / {@link BankilyStatutResponse} avec {@code succes=false}
 * pour ne pas propager de réactif non-géré vers le service appelant.</p>
 */
@Component
public class BankilyClient {

    private static final Logger log = LoggerFactory.getLogger(BankilyClient.class);

    private final WebClient    webClient;
    private final String       merchantId;
    private final Duration     timeout;

    public BankilyClient(WebClient bankilyWebClient,
                         @Qualifier("bankilyMerchantId") String merchantId,
                         @Qualifier("bankilyTimeout")    Duration timeout) {
        this.webClient  = bankilyWebClient;
        this.merchantId = merchantId;
        this.timeout    = timeout;
    }

    // =========================================================================
    //  Initiation d'un paiement (décaissement MFI → wallet client)
    // =========================================================================

    /**
     * Initie un paiement sortant (disbursement) vers le wallet Bankily du bénéficiaire.
     * Utilisé pour décaisser un crédit ou payer un prestataire.
     *
     * @param telephone  numéro Bankily du bénéficiaire (format international : +222XXXXXXXX)
     * @param montant    montant en MRU
     * @param reference  référence unique MFI de la transaction
     * @param motif      libellé affiché au bénéficiaire
     * @return réponse Bankily : référence transaction + statut initial
     */
    public BankilyInitResponse initierPaiement(String telephone,
                                                BigDecimal montant,
                                                String reference,
                                                String motif) {
        log.info("[Bankily] Initiation paiement ref={} tel={} montant={}", reference, telephone, montant);

        Map<String, Object> body = Map.of(
                "merchantId",   merchantId,
                "msisdn",       telephone,
                "amount",       montant,
                "currency",     "MRU",
                "reference",    reference,
                "description",  motif != null ? motif : "Décaissement crédit MFI"
        );

        try {
            BankilyRawInitResponse raw = webClient.post()
                    .uri("/payment/initiate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(BankilyRawInitResponse.class)
                    .timeout(timeout)
                    .block();

            if (raw == null) {
                return echecInit(reference, "EMPTY_RESPONSE", "Réponse vide de Bankily");
            }

            boolean succes = "SUCCESS".equalsIgnoreCase(raw.status())
                          || "PENDING".equalsIgnoreCase(raw.status());

            log.info("[Bankily] Paiement initié ref={} refBankily={} statut={}",
                     reference, raw.transactionId(), raw.status());

            return new BankilyInitResponse(succes, raw.transactionId(), raw.code(), raw.message());

        } catch (WebClientResponseException e) {
            log.error("[Bankily] Erreur HTTP {} initiation paiement ref={} : {}",
                      e.getStatusCode(), reference, e.getResponseBodyAsString());
            return echecInit(reference, "HTTP_" + e.getStatusCode().value(), e.getMessage());
        } catch (Exception e) {
            log.error("[Bankily] Erreur réseau initiation paiement ref={} : {}", reference, e.getMessage());
            return echecInit(reference, "NETWORK_ERROR", e.getMessage());
        }
    }

    // =========================================================================
    //  Initiation d'une collecte (remboursement wallet client → MFI)
    // =========================================================================

    /**
     * Initie une collecte (collection) depuis le wallet Bankily du client.
     * Utilisé pour collecter un remboursement de crédit ou un dépôt d'épargne.
     *
     * @param telephone  numéro Bankily du payeur (format international : +222XXXXXXXX)
     * @param montant    montant en MRU
     * @param reference  référence unique MFI de la transaction
     * @param motif      libellé affiché au payeur sur son téléphone
     * @return réponse Bankily : référence transaction + statut initial
     */
    public BankilyInitResponse initierCollecte(String telephone,
                                                BigDecimal montant,
                                                String reference,
                                                String motif) {
        log.info("[Bankily] Initiation collecte ref={} tel={} montant={}", reference, telephone, montant);

        Map<String, Object> body = Map.of(
                "merchantId",   merchantId,
                "msisdn",       telephone,
                "amount",       montant,
                "currency",     "MRU",
                "reference",    reference,
                "description",  motif != null ? motif : "Remboursement crédit MFI"
        );

        try {
            BankilyRawInitResponse raw = webClient.post()
                    .uri("/collection/initiate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(BankilyRawInitResponse.class)
                    .timeout(timeout)
                    .block();

            if (raw == null) {
                return echecInit(reference, "EMPTY_RESPONSE", "Réponse vide de Bankily");
            }

            boolean succes = "SUCCESS".equalsIgnoreCase(raw.status())
                          || "PENDING".equalsIgnoreCase(raw.status());

            log.info("[Bankily] Collecte initiée ref={} refBankily={} statut={}",
                     reference, raw.transactionId(), raw.status());

            return new BankilyInitResponse(succes, raw.transactionId(), raw.code(), raw.message());

        } catch (WebClientResponseException e) {
            log.error("[Bankily] Erreur HTTP {} initiation collecte ref={} : {}",
                      e.getStatusCode(), reference, e.getResponseBodyAsString());
            return echecInit(reference, "HTTP_" + e.getStatusCode().value(), e.getMessage());
        } catch (Exception e) {
            log.error("[Bankily] Erreur réseau initiation collecte ref={} : {}", reference, e.getMessage());
            return echecInit(reference, "NETWORK_ERROR", e.getMessage());
        }
    }

    // =========================================================================
    //  Consultation du statut d'une transaction
    // =========================================================================

    /**
     * Consulte le statut temps-réel d'une transaction auprès de Bankily.
     * Utilisé pour le polling et pour rafraîchir le statut via l'API REST.
     *
     * @param referenceBankily référence de la transaction chez Bankily
     * @return statut courant + code retour
     */
    public BankilyStatutResponse consulterTransaction(String referenceBankily) {
        log.debug("[Bankily] Consultation statut refBankily={}", referenceBankily);

        try {
            BankilyRawStatutResponse raw = webClient.get()
                    .uri("/transaction/{ref}", referenceBankily)
                    .retrieve()
                    .bodyToMono(BankilyRawStatutResponse.class)
                    .timeout(timeout)
                    .block();

            if (raw == null) {
                return new BankilyStatutResponse(referenceBankily, "UNKNOWN", "EMPTY_RESPONSE",
                        "Réponse vide de Bankily");
            }

            return new BankilyStatutResponse(
                    referenceBankily,
                    raw.status(),
                    raw.code(),
                    raw.message());

        } catch (WebClientResponseException e) {
            log.warn("[Bankily] Erreur HTTP {} consultation refBankily={} : {}",
                     e.getStatusCode(), referenceBankily, e.getResponseBodyAsString());
            return new BankilyStatutResponse(referenceBankily, "UNKNOWN",
                    "HTTP_" + e.getStatusCode().value(), e.getMessage());
        } catch (Exception e) {
            log.error("[Bankily] Erreur réseau consultation refBankily={} : {}",
                      referenceBankily, e.getMessage());
            return new BankilyStatutResponse(referenceBankily, "UNKNOWN", "NETWORK_ERROR", e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static BankilyInitResponse echecInit(String ref, String code, String message) {
        return new BankilyInitResponse(false, null, code, message);
    }

    // ── Records internes (correspondance JSON API Bankily) ────────────

    /**
     * Réponse brute de l'endpoint d'initiation Bankily.
     * Les noms de champs correspondent à la réponse JSON de l'API Bankily.
     */
    private record BankilyRawInitResponse(
            String transactionId,   // référence Bankily
            String status,          // SUCCESS | PENDING | FAILED
            String code,            // code retour opérateur
            String message          // message lisible
    ) {}

    /**
     * Réponse brute de l'endpoint de consultation Bankily.
     */
    private record BankilyRawStatutResponse(
            String transactionId,
            String status,
            String code,
            String message
    ) {}
}
