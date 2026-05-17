package com.pfe.backend.controller;

import com.microfina.entity.Utilisateur;
import com.microfina.security.JwtService;
import com.pfe.backend.service.MobileRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * MobileAuthController — inscription self-service côté mobile.
 *
 * <p>Crée en une transaction : {@link Utilisateur}, {@link com.microfina.entity.Membres},
 * {@link com.microfina.entity.Adresse} (si fournie) et un compte courant {@link com.microfina.entity.CompteEps}.</p>
 */
@Tag(name = "Authentification mobile", description = "Inscription self-service pour l'app mobile")
@RestController
@RequestMapping("/api/v1/auth")
public class MobileAuthController {

    private final MobileRegistrationService registrationService;
    private final JwtService jwtService;

    public MobileAuthController(MobileRegistrationService registrationService,
                                JwtService jwtService) {
        this.registrationService = registrationService;
        this.jwtService = jwtService;
    }

    public record RegisterMobileRequest(
        @NotBlank @Size(min = 4, max = 100) String phone,
        @NotBlank @Size(min = 4, max = 64)  String pin,
        @Size(max = 255) String nomComplet,
        @Size(max = 255) String email,
        @Size(max = 255) String adresse,
        @Size(max = 255) String adresse1,
        @Size(max = 255) String rueMaison,
        @Size(max = 255) String ville,
        @Size(max = 50)  String latitude,
        @Size(max = 50)  String longitude,
        @Size(max = 25)  String codeAgence
    ) {}

    @Operation(summary = "Inscription self-service depuis l'app mobile")
    @PostMapping("/register-mobile")
    @Transactional
    public ResponseEntity<?> registerMobile(@Valid @RequestBody RegisterMobileRequest req) {
        try {
            MobileRegistrationService.AddressInput address = null;
            if (hasAddressPayload(req)) {
                address = new MobileRegistrationService.AddressInput(
                    req.adresse(),
                    req.adresse1(),
                    req.rueMaison(),
                    req.ville(),
                    req.latitude(),
                    req.longitude()
                );
            }

            MobileRegistrationService.RegistrationResult result = registrationService.register(
                req.phone(),
                req.pin(),
                req.nomComplet(),
                req.email(),
                req.codeAgence(),
                address
            );

            Utilisateur u = result.utilisateur();

            var userDetails = User.builder()
                .username(u.getLogin())
                .password(u.getMotDePasseHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_MOBILE_USER")))
                .build();

            String token = jwtService.generateToken(userDetails);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                    "token",        token,
                    "username",     u.getLogin(),
                    "role",         "ROLE_MOBILE_USER",
                    "expiresInMs",  jwtService.getExpirationMs(),
                    "numMembre",    result.numMembre(),
                    "numCompte",    result.numCompte()
                ));
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getReason() != null ? e.getReason() : "Conflit"));
            }
            throw e;
        }
    }

    private static boolean hasAddressPayload(RegisterMobileRequest req) {
        return isNotBlank(req.adresse())
            || isNotBlank(req.adresse1())
            || isNotBlank(req.rueMaison())
            || isNotBlank(req.ville())
            || isNotBlank(req.latitude())
            || isNotBlank(req.longitude());
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
