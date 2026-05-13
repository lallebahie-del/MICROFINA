package com.microfina.security;

import com.microfina.entity.ActionAudit;
import com.microfina.entity.JournalAudit;
import com.pfe.backend.repository.JournalAuditRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * AuditAspect – aspect Spring AOP chargé de la traçabilité des opérations métier.
 *
 * <h2>Fonctionnement</h2>
 *
 * <p>Cet aspect intercepte automatiquement les méthodes de service dans les
 * packages {@code com.microfina.service} et {@code com.microfina.security}
 * dont le nom correspond à une opération d'écriture (voir liste ci-dessous).
 * Pour chaque interception réussie, une entrée est créée dans
 * {@link JournalAudit} et persistée dans une transaction
 * {@code REQUIRES_NEW} distincte.</p>
 *
 * <h2>Méthodes interceptées</h2>
 *
 * <p>Toute méthode dont le nom commence par l'un des préfixes suivants
 * (insensible à la casse) est interceptée :</p>
 * <ul>
 *   <li>Opérations de création : {@code create}, {@code creer}, {@code ajouter},
 *       {@code enregistrer}, {@code soumettre}</li>
 *   <li>Opérations de modification : {@code valider}, {@code debloquer},
 *       {@code deposer}, {@code retirer}, {@code payer}, {@code virer},
 *       {@code encaisser}, {@code cloturer}, {@code rejeter}</li>
 *   <li>Opérations de suppression : {@code supprimer}</li>
 *   <li>Opérations de sécurité : {@code login}, {@code logout}</li>
 * </ul>
 *
 * <h2>Transaction REQUIRES_NEW</h2>
 *
 * <p>L'entrée d'audit est persistée dans une transaction indépendante
 * ({@code REQUIRES_NEW}) afin de survivre à un éventuel rollback de la
 * transaction principale. Ainsi, même en cas d'erreur métier, l'audit
 * reste traçable.</p>
 *
 * <h2>Heuristique de détermination de l'action</h2>
 *
 * <p>L'action est déterminée par le préfixe du nom de la méthode interceptée
 * (voir {@link #determinerAction(String)}) :</p>
 * <ul>
 *   <li>Préfixes "create/creer/ajouter/enregistrer/soumettre" → {@link ActionAudit#CREATE}</li>
 *   <li>Préfixe "supprimer" → {@link ActionAudit#DELETE}</li>
 *   <li>Préfixe "login" → {@link ActionAudit#LOGIN}</li>
 *   <li>Préfixe "logout" → {@link ActionAudit#LOGOUT}</li>
 *   <li>Tout autre préfixe d'écriture → {@link ActionAudit#UPDATE}</li>
 * </ul>
 */
@Aspect
@Component
public class AuditAspect {

    private final JournalAuditRepository auditRepo;

    public AuditAspect(JournalAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Pointcuts — opérations d'écriture dans les services métier
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Intercepte les méthodes d'écriture dans {@code com.microfina.service}.
     *
     * <p>Sont interceptées les méthodes dont le nom commence par un préfixe
     * d'opération d'écriture. L'interception a lieu <em>après</em> le retour
     * normal (pas après exception) pour ne tracer que les opérations réussies.</p>
     *
     * @param joinPoint informations sur la méthode interceptée
     */
    @AfterReturning(
        "(execution(* com.microfina.service.*.*(..)) || execution(* com.pfe.backend.service.*.*(..))) && (" +
        "  execution(* create*(..))     ||" +
        "  execution(* creer*(..))      ||" +
        "  execution(* ajouter*(..))    ||" +
        "  execution(* valider*(..))    ||" +
        "  execution(* enregistrer*(..)) ||" +
        "  execution(* deposer*(..))    ||" +
        "  execution(* retirer*(..))    ||" +
        "  execution(* payer*(..))      ||" +
        "  execution(* debloquer*(..))  ||" +
        "  execution(* virer*(..))      ||" +
        "  execution(* encaisser*(..))  ||" +
        "  execution(* cloturer*(..))   ||" +
        "  execution(* soumettre*(..))  ||" +
        "  execution(* rejeter*(..))    ||" +
        "  execution(* supprimer*(..))  " +
        ")"
    )
    public void auditerOperationService(JoinPoint joinPoint) {
        String nomMethode = joinPoint.getSignature().getName();
        String nomClasse  = joinPoint.getTarget().getClass().getSimpleName();
        enregistrerAudit(nomMethode, nomClasse);
    }

    /**
     * Intercepte les méthodes de login et logout dans {@code com.microfina.security}.
     *
     * <p>Permet de tracer les connexions et déconnexions des utilisateurs
     * indépendamment du mécanisme d'authentification utilisé.</p>
     *
     * @param joinPoint informations sur la méthode interceptée
     */
    @AfterReturning(
        "(execution(* com.microfina.security.*.*(..)) || execution(* com.pfe.backend.controller.*.*(..))) && (" +
        "  execution(* login*(..))  ||" +
        "  execution(* logout*(..)) " +
        ")"
    )
    public void auditerOperationSecurite(JoinPoint joinPoint) {
        String nomMethode = joinPoint.getSignature().getName();
        String nomClasse  = joinPoint.getTarget().getClass().getSimpleName();
        // For login, extract username from request args if security context not yet set
        String login = extraireLogin();
        if ("anonyme".equals(login) || "anonymousUser".equals(login)) {
            for (Object arg : joinPoint.getArgs()) {
                try {
                    java.lang.reflect.Method m = arg.getClass().getMethod("username");
                    Object val = m.invoke(arg);
                    if (val != null) { login = val.toString(); break; }
                } catch (Exception ignored) {}
            }
        }
        enregistrerAuditAvecLogin(nomMethode, nomClasse, login);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Persistance de l'entrée d'audit (transaction REQUIRES_NEW)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Crée et persiste une entrée {@link JournalAudit} dans une transaction
     * indépendante ({@code REQUIRES_NEW}).
     *
     * <p>La transaction {@code REQUIRES_NEW} garantit que l'entrée d'audit
     * est commitée même si la transaction principale est rollbackée.</p>
     *
     * @param nomMethode nom de la méthode interceptée
     * @param nomClasse  nom simple de la classe cible
     */
    public void enregistrerAudit(String nomMethode, String nomClasse) {
        enregistrerAuditAvecLogin(nomMethode, nomClasse, extraireLogin());
    }

    public void enregistrerAuditAvecLogin(String nomMethode, String nomClasse, String login) {
        try {
            JournalAudit entree = new JournalAudit();
            entree.setDateAction(java.time.LocalDateTime.now());
            entree.setUtilisateur(login);
            entree.setAction(determinerAction(nomMethode));
            entree.setEntite(nomClasse);
            auditRepo.save(entree);
        } catch (Exception e) {
            // Ne jamais bloquer l'opération métier à cause de l'audit
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers internes
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Extrait le login de l'utilisateur authentifié depuis le contexte
     * de sécurité Spring.
     *
     * @return login de l'utilisateur ou {@code "anonyme"} si non authentifié
     */
    private String extraireLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonyme";
    }

    /**
     * Détermine l'action d'audit à partir du nom de la méthode interceptée.
     *
     * <p>Heuristique basée sur le préfixe du nom de méthode :</p>
     * <ul>
     *   <li>{@code create*}, {@code creer*}, {@code ajouter*},
     *       {@code enregistrer*}, {@code soumettre*} → {@link ActionAudit#CREATE}</li>
     *   <li>{@code supprimer*} → {@link ActionAudit#DELETE}</li>
     *   <li>{@code login*} → {@link ActionAudit#LOGIN}</li>
     *   <li>{@code logout*} → {@link ActionAudit#LOGOUT}</li>
     *   <li>Tout autre préfixe d'écriture → {@link ActionAudit#UPDATE}</li>
     * </ul>
     *
     * @param nomMethode nom brut de la méthode interceptée
     * @return action d'audit correspondante
     */
    public ActionAudit determinerAction(String nomMethode) {
        if (nomMethode == null) {
            return ActionAudit.UPDATE;
        }
        String nom = nomMethode.toLowerCase();
        if (nom.startsWith("create")
                || nom.startsWith("creer")
                || nom.startsWith("ajouter")
                || nom.startsWith("enregistrer")
                || nom.startsWith("soumettre")) {
            return ActionAudit.CREATE;
        }
        if (nom.startsWith("supprimer")) {
            return ActionAudit.DELETE;
        }
        if (nom.startsWith("login")) {
            return ActionAudit.LOGIN;
        }
        if (nom.startsWith("logout")) {
            return ActionAudit.LOGOUT;
        }
        // Tous les autres préfixes d'écriture (valider, deposer, retirer, payer,
        // debloquer, virer, encaisser, cloturer, rejeter, etc.) → UPDATE
        return ActionAudit.UPDATE;
    }
}
