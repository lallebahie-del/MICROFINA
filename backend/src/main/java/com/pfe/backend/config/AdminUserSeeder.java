package com.pfe.backend.config;

import com.microfina.entity.Agence;
import com.microfina.entity.Role;
import com.microfina.entity.Utilisateur;
import com.microfina.entity.UtilisateurRole;
import com.microfina.entity.UtilisateurRoleId;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.RoleRepository;
import com.pfe.backend.repository.UtilisateurRepository;
import com.pfe.backend.repository.UtilisateurRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * AdminUserSeeder – Seeds the initial administrative user if it doesn't exist.
 *
 * <p>This seeder ensures that an 'admin' user is present in the database with the
 * 'ADMIN' role assigned. It uses BCrypt to hash the password.</p>
 */
@Configuration
public class AdminUserSeeder {

    @Bean
    @Order(10) // Run after other initializations
    public CommandLineRunner seedAdminUser(
            UtilisateurRepository utilisateurRepository,
            RoleRepository roleRepository,
            UtilisateurRoleRepository utilisateurRoleRepository,
            AgenceRepository agenceRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            seed(utilisateurRepository, roleRepository, utilisateurRoleRepository, agenceRepository, passwordEncoder);
        };
    }

    @Transactional
    public void seed(
            UtilisateurRepository utilisateurRepository,
            RoleRepository roleRepository,
            UtilisateurRoleRepository utilisateurRoleRepository,
            AgenceRepository agenceRepository,
            PasswordEncoder passwordEncoder) {

        String login = "admin";
        String rawPassword = "Admin@1234";
        String roleCode = "ADMIN";

        // 1. Check if user already exists
        if (utilisateurRepository.existsByLogin(login)) {
            return;
        }

        // 2. Find or create the ADMIN role
        Role adminRole = roleRepository.findByCodeRole(roleCode)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setCodeRole(roleCode);
                    role.setLibelle("Administrateur système");
                    role.setDescription("Accès complet à toutes les fonctionnalités");
                    role.setVersion(0);
                    return roleRepository.save(role);
                });

        // 3. Find the default agency (NKC from Liquibase seeds)
        Agence agence = agenceRepository.findById("NKC").orElse(null);

        // 4. Create the admin user
        Utilisateur admin = new Utilisateur();
        admin.setLogin(login);
        admin.setMotDePasseHash(passwordEncoder.encode(rawPassword));
        admin.setNomComplet("Administrateur Système");
        admin.setEmail("admin@microfina.com");
        admin.setActif(true);
        admin.setNombreEchecs(0);
        admin.setVersion(0);
        admin.setAgence(agence);
        admin.setDateExpirationCompte(LocalDate.now().plusYears(10));

        admin = utilisateurRepository.save(admin);

        // 5. Assign the role via the join table
        UtilisateurRoleId utilisateurRoleId = new UtilisateurRoleId(admin.getId(), adminRole.getId());
        UtilisateurRole utilisateurRole = new UtilisateurRole(utilisateurRoleId, admin, adminRole);
        
        utilisateurRoleRepository.save(utilisateurRole);

        System.out.println(">>> SEEDER: Admin user created successfully with login: " + login);
    }
}
