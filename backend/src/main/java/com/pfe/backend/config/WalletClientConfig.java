package com.pfe.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * WalletClientConfig — configuration du {@link WebClient} dédié à l'API Bankily.
 *
 * <h2>Propriétés applicatives</h2>
 * <pre>
 * microfina.wallet.bankily.base-url     = https://api.bankily.mr/v1
 * microfina.wallet.bankily.api-key      = &lt;clé API Bankily&gt;
 * microfina.wallet.bankily.merchant-id  = &lt;identifiant marchand&gt;
 * microfina.wallet.bankily.timeout-sec  = 30
 * microfina.wallet.bankily.hmac-secret  = &lt;secret HMAC pour vérifier les callbacks&gt;
 * </pre>
 *
 * <p>Le bean {@code bankilyWebClient} est injecté dans {@link com.pfe.backend.wallet.BankilyClient}.
 * Le {@code hmacSecret} est exposé en tant que bean {@code String} qualifié pour être
 * injecté dans le service de vérification des callbacks.</p>
 */
@Configuration
public class WalletClientConfig {

    @Value("${microfina.wallet.bankily.base-url:https://api.bankily.mr/v1}")
    private String baseUrl;

    @Value("${microfina.wallet.bankily.api-key:BANKILY_API_KEY_NOT_SET}")
    private String apiKey;

    @Value("${microfina.wallet.bankily.merchant-id:MICROFINA_MFI}")
    private String merchantId;

    @Value("${microfina.wallet.bankily.timeout-sec:30}")
    private int timeoutSec;

    /**
     * Identifiant marchand exposé comme bean pour être injecté dans BankilyClient.
     */
    @Bean(name = "bankilyMerchantId")
    public String bankilyMerchantId() {
        return merchantId;
    }

    /**
     * Durée de timeout exposée comme bean (utilisée par BankilyClient pour
     * {@code .timeout()} sur les requêtes réactives).
     */
    @Bean(name = "bankilyTimeout")
    public Duration bankilyTimeout() {
        return Duration.ofSeconds(timeoutSec);
    }

    /**
     * WebClient préconfiguré pour l'API Bankily :
     * <ul>
     *   <li>Base URL fixée à la valeur de {@code microfina.wallet.bankily.base-url}</li>
     *   <li>Header {@code X-Api-Key} positionné avec la clé API</li>
     *   <li>Header {@code X-Merchant-Id} positionné avec l'identifiant marchand</li>
     *   <li>Content-Type et Accept forcés à {@code application/json}</li>
     * </ul>
     *
     * <p>Note : le codec par défaut de Spring WebFlux est utilisé (pas de configuration
     * Reactor Netty explicite) — adapté à la charge de l'API Bankily (transactions
     * unitaires, pas de streaming).</p>
     */
    @Bean
    public WebClient bankilyWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT,       MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Api-Key",              apiKey)
                .defaultHeader("X-Merchant-Id",          merchantId)
                .build();
    }
}
