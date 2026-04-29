package com.pfe.backend.repository;

import com.microfina.entity.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JobExecutionRepository — accès JPA à la table job_execution.
 */
@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {

    /** Retourne les 50 dernières exécutions d'un job donné, triées par date décroissante. */
    List<JobExecution> findTop50ByNomJobOrderByDateDebutDesc(String nomJob);

    /** Retourne les 100 dernières exécutions tous jobs confondus. */
    List<JobExecution> findTop100ByOrderByDateDebutDesc();
}
