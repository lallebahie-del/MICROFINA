package com.pfe.backend.service;

import com.pfe.backend.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Arrays;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BackupService — sauvegarde et restauration de la base de données SQL Server.
 *
 * <p>Utilise {@code BACKUP DATABASE} et {@code RESTORE DATABASE} de SQL Server.
 * Le fichier de sauvegarde est nommé {@code microfina_YYYYMMDD_HHmmss.bak}
 * et stocké dans le répertoire configuré par {@code app.backup.dir}.</p>
 *
 * <p>ATTENTION : la restauration arrête toutes les connexions actives
 * ({@code ALTER DATABASE ... SET SINGLE_USER WITH ROLLBACK IMMEDIATE})
 * avant de lancer le RESTORE. En production, planifier les fenêtres de
 * maintenance appropriées.</p>
 */
@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final JdbcTemplate jdbc;
    private final Environment env;

    @Value("${app.backup.dir:./backups}")
    private String backupDir;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    public BackupService(JdbcTemplate jdbc, Environment env) {
        this.jdbc = jdbc;
        this.env = env;
    }

    /**
     * Déclenche une sauvegarde complète de la base de données SQL Server.
     *
     * @return chemin absolu du fichier .bak créé
     */
    public String backup() {
        if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
            throw new BusinessException("Backup non supporté en mode test");
        }
        String dbName = extractDbName();
        String filename = "microfina_" + LocalDateTime.now().format(FMT) + ".bak";
        Path dir = Paths.get(backupDir).toAbsolutePath();

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException("Impossible de créer le répertoire de sauvegarde: " + dir);
        }

        Path target = dir.resolve(filename);
        String sql = String.format(
            "BACKUP DATABASE [%s] TO DISK = N'%s' WITH NOFORMAT, NOINIT, " +
            "NAME = N'%s-Full Backup', SKIP, NOREWIND, NOUNLOAD, STATS = 10",
            dbName, target.toString().replace("'", "''"), dbName
        );

        log.info("[Backup] Démarrage sauvegarde → {}", target);
        try {
            jdbc.execute(sql);
            log.info("[Backup] Sauvegarde terminée → {}", target);
        } catch (Exception e) {
            log.error("[Backup] Échec sauvegarde : {}", e.getMessage());
            throw new BusinessException("Échec de la sauvegarde SQL Server : " + e.getMessage());
        }
        return target.toString();
    }

    /**
     * Liste les fichiers .bak disponibles dans le répertoire de sauvegarde.
     *
     * @return liste des noms de fichiers de sauvegarde (tri décroissant)
     */
    public java.util.List<String> listerSauvegardes() {
        Path dir = Paths.get(backupDir).toAbsolutePath();
        if (!Files.exists(dir)) return java.util.Collections.emptyList();

        try {
            return Files.list(dir)
                .filter(p -> p.toString().endsWith(".bak"))
                .map(p -> p.getFileName().toString())
                .sorted(java.util.Comparator.reverseOrder())
                .collect(java.util.stream.Collectors.toList());
        } catch (IOException e) {
            throw new BusinessException("Erreur lors de la lecture du répertoire de sauvegarde");
        }
    }

    /**
     * Restaure la base de données à partir d'un fichier .bak.
     *
     * <p>ATTENTION : opération destructive — interrompt toutes les sessions actives.</p>
     *
     * @param filename nom du fichier .bak (sans chemin)
     */
    public void restore(String filename) {
        if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
            throw new BusinessException("Backup non supporté en mode test");
        }
        String dbName = extractDbName();
        Path source = Paths.get(backupDir).toAbsolutePath().resolve(filename);

        if (!Files.exists(source)) {
            throw new BusinessException("Fichier de sauvegarde introuvable : " + filename);
        }
        if (!filename.endsWith(".bak")) {
            throw new BusinessException("Nom de fichier invalide. Extension attendue : .bak");
        }

        log.warn("[Restore] RESTAURATION de {} depuis {}", dbName, source);

        // Forcer déconnexion de toutes les sessions
        String killSql = String.format(
            "ALTER DATABASE [%s] SET SINGLE_USER WITH ROLLBACK IMMEDIATE", dbName);

        String restoreSql = String.format(
            "RESTORE DATABASE [%s] FROM DISK = N'%s' WITH FILE = 1, NOUNLOAD, REPLACE, STATS = 5",
            dbName, source.toString().replace("'", "''")
        );

        String multiUser = String.format(
            "ALTER DATABASE [%s] SET MULTI_USER", dbName);

        try {
            jdbc.execute(killSql);
            jdbc.execute(restoreSql);
            jdbc.execute(multiUser);
            log.info("[Restore] Restauration terminée depuis {}", source);
        } catch (Exception e) {
            log.error("[Restore] Échec restauration : {}", e.getMessage());
            throw new BusinessException("Échec de la restauration SQL Server : " + e.getMessage());
        }
    }

    private String extractDbName() {
        // Parse "jdbc:sqlserver://host:port;databaseName=MICROFINA;..."
        if (datasourceUrl != null && datasourceUrl.contains("databaseName=")) {
            int start = datasourceUrl.indexOf("databaseName=") + "databaseName=".length();
            int end = datasourceUrl.indexOf(";", start);
            return end > start ? datasourceUrl.substring(start, end) : datasourceUrl.substring(start);
        }
        return "MICROFINA";
    }
}
