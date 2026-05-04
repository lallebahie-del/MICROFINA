package com.pfe.backend.repository;

import com.microfina.entity.OperationWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * OperationWalletRepository — accès Spring Data JPA à la table {@code OperationWallet}.
 *
 * <p>DDL source of truth : P10-601.</p>
 */
@Repository
public interface OperationWalletRepository extends JpaRepository<OperationWallet, Long> {

    /** Recherche par référence interne MFI (unique). */
    Optional<OperationWallet> findByReferenceMfi(String referenceMfi);

    /** Recherche par référence Bankily (retournée par l'API lors de l'initiation). */
    Optional<OperationWallet> findByReferenceBankily(String referenceBankily);

    /** Historique des opérations d'un membre, plus récent en premier. */
    @Query("""
        SELECT o FROM OperationWallet o
        WHERE o.numMembre = :numMembre
        ORDER BY o.dateOperation DESC, o.id DESC
        """)
    List<OperationWallet> findByNumMembre(@Param("numMembre") String numMembre);

    /** Opérations liées à un crédit (déblocage + remboursements). */
    @Query("""
        SELECT o FROM OperationWallet o
        WHERE o.idCredit = :idCredit
        ORDER BY o.dateOperation DESC, o.id DESC
        """)
    List<OperationWallet> findByIdCredit(@Param("idCredit") Long idCredit);

    /**
     * Opérations EN_ATTENTE créées avant {@code avantDate} — utilisées par le
     * job d'expiration pour passer les opérations non confirmées à EXPIRE.
     */
    @Query("""
        SELECT o FROM OperationWallet o
        WHERE o.statut = 'EN_ATTENTE'
          AND o.dateOperation < :avantDate
        """)
    List<OperationWallet> findEnAttenteAvant(@Param("avantDate") LocalDate avantDate);

    /**
     * Recherche multi-critères par agence, type et statut.
     * Les paramètres nuls sont ignorés dans le filtre.
     */
    @Query("""
        SELECT o FROM OperationWallet o
        WHERE (:codeAgence   IS NULL OR o.codeAgence   = :codeAgence)
          AND (:typeOperation IS NULL OR o.typeOperation = :typeOperation)
          AND (:statut        IS NULL OR o.statut        = :statut)
        ORDER BY o.dateOperation DESC, o.id DESC
        """)
    List<OperationWallet> rechercher(
            @Param("codeAgence")    String codeAgence,
            @Param("typeOperation") String typeOperation,
            @Param("statut")        String statut);
}
