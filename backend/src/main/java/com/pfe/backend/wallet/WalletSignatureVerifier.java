package com.pfe.backend.wallet;

import com.pfe.backend.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Service de vérification de la signature HMAC-SHA256 des webhooks Bankily.
 *
 * <p>Bankily signe la charge utile JSON avec le secret partagé configuré dans
 * {@code app.bankily.hmac-secret} et envoie la signature hexadécimale dans
 * le header {@code X-Bankily-Signature}.  Ce service recompute la signature
 * côté serveur et compare avec {@link MessageDigest#isEqual} (temps constant)
 * pour prévenir les attaques temporelles.</p>
 *
 * <h3>Utilisation</h3>
 * <pre>{@code
 * walletSignatureVerifier.verify(signatureHeader, rawBody);
 * }</pre>
 *
 * @see BankilySignatureFilter
 */
@Service
public class WalletSignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(WalletSignatureVerifier.class);
    private static final String ALGORITHM = "HmacSHA256";

    @Value("${app.bankily.hmac-secret:changeme-in-production}")
    private String hmacSecret;

    /**
     * Vérifie que {@code signature} correspond au HMAC-SHA256 de {@code payload}
     * calculé avec le secret configuré.
     *
     * @param signature  header {@code X-Bankily-Signature} (hex, peut être {@code null})
     * @param payload    corps de la requête HTTP brut (bytes UTF-8)
     * @throws BusinessException si la signature est absente ou invalide
     */
    public void verify(String signature, byte[] payload) {
        if (signature == null || signature.isBlank()) {
            log.warn("[Wallet] Callback reçu sans header X-Bankily-Signature");
            throw new BusinessException("Signature HMAC absente — requête rejetée");
        }

        String expected = computeHmac(payload);

        byte[] sigBytes      = hexToBytes(signature.strip());
        byte[] expectedBytes = hexToBytes(expected);

        if (!MessageDigest.isEqual(sigBytes, expectedBytes)) {
            log.warn("[Wallet] Signature HMAC invalide — requête rejetée");
            throw new BusinessException("Signature HMAC invalide — requête rejetée");
        }

        log.debug("[Wallet] Signature HMAC vérifiée avec succès");
    }

    /**
     * Calcule HMAC-SHA256(payload, secret) et retourne le résultat en hexadécimal
     * minuscule.
     *
     * @param payload octets de la charge utile
     * @return empreinte hex (64 caractères)
     */
    public String computeHmac(byte[] payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    hmacSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(keySpec);
            byte[] raw = mac.doFinal(payload);
            return HexFormat.of().formatHex(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Erreur init HMAC-SHA256", e);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static byte[] hexToBytes(String hex) {
        try {
            return HexFormat.of().parseHex(hex);
        } catch (IllegalArgumentException e) {
            // Si la chaîne n'est pas un hex valide → comparaison garantie échouer
            return new byte[0];
        }
    }
}
