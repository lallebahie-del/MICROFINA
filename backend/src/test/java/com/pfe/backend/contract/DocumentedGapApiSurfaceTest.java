package com.pfe.backend.contract;

import com.pfe.backend.controller.AgenceController;
import com.pfe.backend.controller.BudgetController;
import com.pfe.backend.controller.ComptabiliteController;
import com.pfe.backend.controller.TypeGarantieController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contrat aligné sur {@code docs/FONCTIONNALITES-NON-IMPLEMENTEES.md} :
 * tant que les fonctionnalités listées ne sont pas livrées en API, ces tests
 * doivent passer. Lorsqu’un endpoint manquant est ajouté, mettre à jour ce
 * fichier et le document pour éviter une dette de spécification.
 */
class DocumentedGapApiSurfaceTest {

    @Nested
    @DisplayName("BudgetController — pas d’API lignes / mouvements")
    class BudgetApi {

        @Test
        @DisplayName("Aucun chemin d’endpoint ne couvre ligne ou mouvement budgétaire")
        void noLigneOrMouvementPaths() {
            List<String> paths = allPaths(BudgetController.class);
            assertThat(paths)
                    .noneMatch(p -> p.toLowerCase(Locale.ROOT).contains("ligne"))
                    .noneMatch(p -> p.toLowerCase(Locale.ROOT).contains("mouvement"));
        }
    }

    @Nested
    @DisplayName("ComptabiliteController — lecture + lettrage seulement")
    class ComptaApi {

        @Test
        @DisplayName("Pas de POST / PUT / DELETE sur les écritures (pas de saisie manuelle API)")
        void noMutatingHttpMethodsBeyondPatchLettrer() {
            assertThat(methodsWithAnnotation(ComptabiliteController.class, PostMapping.class)).isEmpty();
            assertThat(methodsWithAnnotation(ComptabiliteController.class, PutMapping.class)).isEmpty();
            assertThat(methodsWithAnnotation(ComptabiliteController.class, DeleteMapping.class)).isEmpty();
            assertThat(methodsWithAnnotation(ComptabiliteController.class, PatchMapping.class))
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("Référentiels en lecture seule")
    class ReferentielReadOnly {

        @Test
        @DisplayName("AgenceController — pas de POST / PUT / PATCH / DELETE")
        void agenceReadOnly() {
            assertThat(mutatingMappings(AgenceController.class)).isEmpty();
        }

        @Test
        @DisplayName("TypeGarantieController — pas de POST / PUT / PATCH / DELETE")
        void typeGarantieReadOnly() {
            assertThat(mutatingMappings(TypeGarantieController.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Produits islamiques — pas de contrôleur REST dédié")
    class ProduitIslamic {

        @Test
        @DisplayName("Classe ProduitIslamicController absente du module controller")
        void noDedicatedControllerClass() {
            assertThatThrownBy(() -> Class.forName("com.pfe.backend.controller.ProduitIslamicController"))
                    .isInstanceOf(ClassNotFoundException.class);
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private static List<Method> methodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> ann) {
        List<Method> out = new ArrayList<>();
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isSynthetic()) {
                continue;
            }
            if (m.getAnnotation(ann) != null) {
                out.add(m);
            }
        }
        return out;
    }

    /** POST, PUT, PATCH, DELETE sur la classe (annotations Spring MVC). */
    private static List<String> mutatingMappings(Class<?> controllerClass) {
        List<String> out = new ArrayList<>();
        String base = classLevelPath(controllerClass);
        for (Method m : controllerClass.getDeclaredMethods()) {
            if (m.isSynthetic()) {
                continue;
            }
            for (Annotation a : m.getAnnotations()) {
                if (a instanceof PostMapping pm) {
                    out.add(base + firstPath(pm.value()));
                } else if (a instanceof PutMapping pm) {
                    out.add(base + firstPath(pm.value()));
                } else if (a instanceof PatchMapping pm) {
                    out.add(base + firstPath(pm.value()));
                } else if (a instanceof DeleteMapping dm) {
                    out.add(base + firstPath(dm.value()));
                }
            }
        }
        return out;
    }

    private static List<String> allPaths(Class<?> controllerClass) {
        List<String> out = new ArrayList<>();
        String base = classLevelPath(controllerClass);
        for (Method m : controllerClass.getDeclaredMethods()) {
            if (m.isSynthetic()) {
                continue;
            }
            for (Annotation a : m.getAnnotations()) {
                if (a instanceof GetMapping gm) {
                    out.add(base + firstPath(gm.value()));
                } else if (a instanceof PostMapping pm) {
                    out.add(base + firstPath(pm.value()));
                } else if (a instanceof PutMapping pm) {
                    out.add(base + firstPath(pm.value()));
                } else if (a instanceof PatchMapping pm) {
                    out.add(base + firstPath(pm.value()));
                } else if (a instanceof DeleteMapping dm) {
                    out.add(base + firstPath(dm.value()));
                } else if (a instanceof RequestMapping rm) {
                    if (rm.path().length > 0) {
                        for (String p : rm.path()) {
                            out.add(base + p);
                        }
                    } else if (rm.value().length > 0) {
                        for (String p : rm.value()) {
                            out.add(base + p);
                        }
                    }
                }
            }
        }
        return out;
    }

    private static String classLevelPath(Class<?> controllerClass) {
        RequestMapping rm = controllerClass.getAnnotation(RequestMapping.class);
        if (rm == null) {
            return "";
        }
        if (rm.path().length > 0) {
            return normalizeBase(rm.path()[0]);
        }
        if (rm.value().length > 0) {
            return normalizeBase(rm.value()[0]);
        }
        return "";
    }

    private static String normalizeBase(String base) {
        if (base == null || base.isEmpty()) {
            return "";
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private static String firstPath(String[] paths) {
        if (paths == null || paths.length == 0) {
            return "";
        }
        return paths[0];
    }
}
