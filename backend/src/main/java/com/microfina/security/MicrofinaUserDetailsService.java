package com.microfina.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * MicrofinaUserDetailsService – service Spring Security de chargement des utilisateurs.
 *
 * <h2>Stratégie d'authentification à deux niveaux</h2>
 *
 * <p>Ce service implémente une stratégie de <em>fallback</em> : il tente d'abord
 * de charger l'utilisateur depuis la nouvelle table {@code Utilisateur} (Phase 9),
 * puis se rabat sur l'ancienne table {@code AGENT_CREDIT} pour assurer la
 * rétrocompatibilité avec les comptes existants.</p>
 *
 * <h2>Modèle d'autorisation — Utilisateur (Phase 9)</h2>
 *
 * <p>Les authorities retournées sont les {@code code_privilege} des privilèges
 * associés aux rôles de l'utilisateur, sans préfixe {@code ROLE_}.
 * Exemples : {@code CREATE_CREDIT}, {@code VALIDATE_CREDIT}, {@code VIEW_REPORTS}.</p>
 *
 * <p>La chaîne de jointure utilisée est :</p>
 * <pre>
 *   Utilisateur → UtilisateurRole → Role → RolePrivilege → Privilege
 * </pre>
 *
 * <h2>Modèle d'autorisation — AGENT_CREDIT (fallback)</h2>
 *
 * <p>Le champ {@code tfunction} est mappé vers des privileges granulaires :</p>
 *
 * <table border="1">
 *   <tr><th>tfunction</th><th>Authorities accordées</th></tr>
 *   <tr><td>ADMIN</td><td>CREATE_CREDIT, VALIDATE_CREDIT, VIEW_REPORTS, POST_REGLEMENT,
 *       DEBLOQUER_CREDIT, VIRER_FONDS, ENCAISSER_CHEQUE, CLOTURER_JOURNEE,
 *       MANAGE_UTILISATEURS, MANAGE_ROLES</td></tr>
 *   <tr><td>COMITE</td><td>VALIDATE_CREDIT, VIEW_REPORTS</td></tr>
 *   <tr><td>AGENT_CREDIT</td><td>CREATE_CREDIT, VIEW_REPORTS, POST_REGLEMENT</td></tr>
 *   <tr><td>(autre)</td><td>VIEW_REPORTS</td></tr>
 * </table>
 *
 * <h2>Identifiants de test (seedés par test-data-seeder.xml SEED-003)</h2>
 *
 * <table border="1">
 *   <tr><th>Utilisateur</th><th>Mot de passe</th><th>Source</th></tr>
 *   <tr><td>admin</td><td>Admin@1234</td><td>AGENT_CREDIT (fallback)</td></tr>
 *   <tr><td>aminata.sow</td><td>Agent@1234</td><td>AGENT_CREDIT (fallback)</td></tr>
 *   <tr><td>oumar.ba</td><td>Comite@1234</td><td>AGENT_CREDIT (fallback)</td></tr>
 * </table>
 */
@Service
public class MicrofinaUserDetailsService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Point d'entrée principal Spring Security.
     *
     * <p>Tente d'abord {@link #loadFromUtilisateur(String)}, puis
     * {@link #loadFromAgentCredit(String)} en cas d'échec. Lève
     * {@link UsernameNotFoundException} si les deux sources échouent.</p>
     *
     * @param username login saisi par l'utilisateur
     * @return {@link UserDetails} avec les authorities chargées
     * @throws UsernameNotFoundException si aucun compte actif n'est trouvé
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return loadFromUtilisateur(username);
        } catch (UsernameNotFoundException premierEchec) {
            // Fallback vers AGENT_CREDIT pour la rétrocompatibilité
            try {
                return loadFromAgentCredit(username);
            } catch (UsernameNotFoundException deuxiemeEchec) {
                throw new UsernameNotFoundException(
                    "Aucun compte actif trouvé pour l'identifiant : " + username);
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Chargement depuis la table Utilisateur (Phase 9)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Charge un {@link UserDetails} depuis la table {@code Utilisateur}.
     *
     * <p>Effectue une jointure complète pour récupérer tous les privilèges
     * associés via la chaîne : Utilisateur → UtilisateurRole → Role →
     * RolePrivilege → Privilege.</p>
     *
     * <p>Seuls les comptes avec {@code actif = true} peuvent s'authentifier.</p>
     *
     * @param username login de l'utilisateur recherché
     * @return {@link UserDetails} avec les authorities (code_privilege)
     * @throws UsernameNotFoundException si l'utilisateur est introuvable ou inactif
     */
    private UserDetails loadFromUtilisateur(String username) throws UsernameNotFoundException {

        // Chargement de l'utilisateur (login + mot de passe + statut actif)
        String sqlUtilisateur =
            "SELECT u.id, u.login, u.mot_de_passe_hash, u.actif " +
            "FROM Utilisateur u " +
            "WHERE u.login = :login";

        Object[] rowUtilisateur;
        try {
            rowUtilisateur = (Object[]) em.createNativeQuery(sqlUtilisateur)
                                          .setParameter("login", username)
                                          .getSingleResult();
        } catch (NoResultException e) {
            throw new UsernameNotFoundException(
                "Utilisateur introuvable dans la table Utilisateur : " + username);
        }

        Long   idUtilisateur = ((Number) rowUtilisateur[0]).longValue();
        String login         = (String) rowUtilisateur[1];
        String motDePasseHash = (String) rowUtilisateur[2];
        Object  actifRaw     = rowUtilisateur[3];
        boolean actif        = actifRaw instanceof Boolean b ? b
                             : actifRaw instanceof Number  n && n.intValue() != 0;

        if (!actif) {
            throw new UsernameNotFoundException(
                "Le compte Utilisateur " + username + " est désactivé.");
        }

        // Chargement des privilèges via la chaîne de jointure complète
        String sqlPrivileges =
            "SELECT DISTINCT p.code_privilege " +
            "FROM UtilisateurRole ur " +
            "INNER JOIN RolePrivilege rp ON rp.id_role = ur.id_role " +
            "INNER JOIN Privilege p      ON p.id       = rp.id_privilege " +
            "WHERE ur.id_utilisateur = :idUtilisateur " +
            "  AND p.code_privilege IS NOT NULL";

        @SuppressWarnings("unchecked")
        List<String> codePrivileges = (List<String>) em.createNativeQuery(sqlPrivileges)
                                                        .setParameter("idUtilisateur", idUtilisateur)
                                                        .getResultList();

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String code : codePrivileges) {
            if (code != null && !code.isBlank()) {
                // Phase 11 : les @PreAuthorize utilisent le préfixe PRIV_
                String authority = code.startsWith("PRIV_") ? code : "PRIV_" + code;
                authorities.add(new SimpleGrantedAuthority(authority));
            }
        }

        return User.builder()
                   .username(login)
                   .password(motDePasseHash)   // hash BCrypt déjà encodé
                   .authorities(authorities)
                   .accountExpired(false)
                   .accountLocked(false)
                   .credentialsExpired(false)
                   .disabled(false)
                   .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Chargement depuis AGENT_CREDIT (fallback rétrocompatibilité)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Charge un {@link UserDetails} depuis la table {@code AGENT_CREDIT}.
     *
     * <p>Fallback utilisé quand l'utilisateur n'existe pas encore dans la
     * nouvelle table {@code Utilisateur}. Le champ {@code tfunction} est
     * converti en liste de privileges granulaires (sans préfixe ROLE_).</p>
     *
     * <p>Seuls les agents avec {@code actif = 'O'} peuvent s'authentifier.</p>
     *
     * @param username usercode de l'agent recherché
     * @return {@link UserDetails} avec les authorities mappées depuis tfunction
     * @throws UsernameNotFoundException si l'agent est introuvable ou inactif
     */
    private UserDetails loadFromAgentCredit(String username) throws UsernameNotFoundException {

        String sql =
            "SELECT a.usercode, a.password, a.tfunction, a.actif " +
            "FROM AGENT_CREDIT a " +
            "WHERE a.usercode = :usercode";

        Object[] row;
        try {
            row = (Object[]) em.createNativeQuery(sql)
                               .setParameter("usercode", username)
                               .getSingleResult();
        } catch (NoResultException e) {
            throw new UsernameNotFoundException(
                "Aucun agent trouvé dans AGENT_CREDIT avec le usercode : " + username);
        }

        String dbUsercode = (String) row[0];
        String dbPassword = (String) row[1]; // hash BCrypt
        String tfunction  = row[2] != null ? (String) row[2] : "";
        String actif      = row[3] != null ? (String) row[3] : "N";

        if (!"O".equalsIgnoreCase(actif)) {
            throw new UsernameNotFoundException(
                "Le compte AGENT_CREDIT " + username + " est désactivé (actif = " + actif + ").");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String code : mapTfunctionToPrivileges(tfunction)) {
            authorities.add(new SimpleGrantedAuthority(code));
        }

        return User.builder()
                   .username(dbUsercode)
                   .password(dbPassword)   // hash BCrypt déjà encodé
                   .authorities(authorities)
                   .accountExpired(false)
                   .accountLocked(false)
                   .credentialsExpired(false)
                   .disabled(false)
                   .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers internes
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Convertit le champ {@code tfunction} d'AGENT_CREDIT en liste de privileges
     * granulaires (sans préfixe ROLE_), conformément au modèle Phase 9.
     *
     * <table border="1">
     *   <tr><th>tfunction</th><th>Privileges accordés</th></tr>
     *   <tr><td>ADMIN</td><td>Tous les privileges du seed</td></tr>
     *   <tr><td>COMITE</td><td>VALIDATE_CREDIT, VIEW_REPORTS</td></tr>
     *   <tr><td>AGENT_CREDIT</td><td>CREATE_CREDIT, VIEW_REPORTS, POST_REGLEMENT</td></tr>
     *   <tr><td>(défaut)</td><td>VIEW_REPORTS</td></tr>
     * </table>
     *
     * @param tfunction valeur brute de la colonne tfunction
     * @return liste des codes de privilege accordés
     */
    private List<String> mapTfunctionToPrivileges(String tfunction) {
        // Phase 11 : toutes les authorities sont préfixées PRIV_
        return switch (tfunction.toUpperCase()) {
            case "ADMIN" -> List.of(
                "PRIV_CREATE_CREDIT",
                "PRIV_VALIDATE_CREDIT",
                "PRIV_DISBURSE_CREDIT",
                "PRIV_REJECT_CREDIT",
                "PRIV_VIEW_REPORTS",
                "PRIV_EXPORT_REPORTS",
                "PRIV_POST_REGLEMENT",
                "PRIV_BANK_OPERATION",
                "PRIV_MANAGE_CHEQUE",
                "PRIV_OPEN_COMPTE_EPS",
                "PRIV_DEPOSIT_EPARGNE",
                "PRIV_WITHDRAW_EPARGNE",
                "PRIV_CREATE_BUDGET",
                "PRIV_VALIDATE_BUDGET",
                "PRIV_MANAGE_BUDGET",
                "PRIV_RECORD_MOVEMENT",
                "PRIV_CLOSE_DAY",
                "PRIV_MANAGE_USERS",
                "PRIV_MANAGE_BANK",
                "PRIV_VIEW_AUDIT"
            );
            case "COMITE"       -> List.of(
                "PRIV_VALIDATE_CREDIT",
                "PRIV_REJECT_CREDIT",
                "PRIV_VIEW_REPORTS"
            );
            case "AGENT_CREDIT" -> List.of(
                "PRIV_CREATE_CREDIT",
                "PRIV_DISBURSE_CREDIT",
                "PRIV_POST_REGLEMENT",
                "PRIV_OPEN_COMPTE_EPS",
                "PRIV_VIEW_REPORTS"
            );
            default -> List.of("PRIV_VIEW_REPORTS");
        };
    }
}
