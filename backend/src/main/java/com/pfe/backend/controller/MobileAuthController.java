package com.pfe.backend.controller;

import com.microfina.entity.Utilisateur;
import com.microfina.security.JwtService;
import com.pfe.backend.repository.UtilisateurRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MobileAuthController — inscription self-service côté mobile.
 *
 * <p>Endpoint :
 * <pre>
 *   POST /api/v1/auth/register-mobile
 * </pre>
 * </p>
 *
 * <p>Flow :
 *   <ol>
 *     <li>Un utilisateur saisit téléphone + PIN dans l'app mobile.</li>
 *     <li>Le téléphone devient le {@code login} du compte Spring Security.</li>
 *     <li>Le PIN est haché en BCrypt et stocké comme mot de passe.</li>
 *     <li>Un JWT est immédiatement renvoyé pour que l'utilisateur soit connecté.</li>
 *   </ol>
 * </p>
 *
 * <p>⚠ Pas d'OTP ici (à brancher sur un provider SMS plus tard). En production,
 * il faudrait au minimum vérifier le téléphone par SMS avant d'activer le compte.</p>
 */
@Tag(name = "Authentification mobile", description = "Inscription self-service pour l'app mobile")
@RestController
@RequestMapping("/api/v1/auth")
public class MobileAuthController {

    private final UtilisateurRepository utilisateurRepo;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;

    public MobileAuthController(UtilisateurRepository utilisateurRepo,
                                PasswordEncoder passwordEncoder,
                                JwtService jwtService) {
        this.utilisateurRepo = utilisateurRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
    }

    public record RegisterMobileRequest(
        @NotBlank @Size(min = 4, max = 100) String phone,
        @NotBlank @Size(min = 4, max = 64)  String pin,
        @Size(max = 255) String nomComplet,
        @Size(max = 255) String email
    ) {}

    @Operation(summary = "Inscription self-service depuis l'app mobile")
    @PostMapping("/register-mobile")
    @Transactional
    public ResponseEntity<?> registerMobile(@Valid @RequestBody RegisterMobileRequest req) {
        if (utilisateurRepo.findByLogin(req.phone()).isPresent()) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Un compte existe déjà pour ce numéro."));
        }

        Utilisateur u = new Utilisateur();
        u.setLogin(req.phone());
        u.setMotDePasseHash(passwordEncoder.encode(req.pin()));
        u.setNomComplet(req.nomComplet() != null && !req.nomComplet().isBlank()
            ? req.nomComplet() : req.phone());
        u.setEmail(req.email());
        u.setTelephone(req.phone());
        u.setActif(Boolean.TRUE);
        u.setNombreEchecs(0);
        utilisateurRepo.save(u);

        // Génère un JWT immédiat (un peu comme /login) avec une authority par défaut.
        // Le user pourra ensuite se voir affecter des rôles par l'admin.
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
                "expiresInMs",  jwtService.getExpirationMs()
            ));
    }
}
