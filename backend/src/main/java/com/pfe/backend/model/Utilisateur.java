package com.pfe.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    @Id
    @Column(name = "CODE_TIERS")
    private String codeTiers;

    @Column(name = "Nomutil")
    private String nomutil;

    @Column(name = "Password")
    private String password;

    @Column(name = "Pactif")
    private Boolean actif;

    @ManyToOne
    @JoinColumn(name = "ID_PROFIL")
    private Profil profil;

    @Column(name = "AGENCE")
    private String codeAgence;
}
