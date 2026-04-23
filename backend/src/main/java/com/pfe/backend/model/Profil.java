package com.pfe.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "Profil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profil {
    @Id
    private Long id;

    @Column(name = "Intitule")
    private String intitule;

    @Column(name = "DESCRIPTION")
    private String description;

    @Version
    @Column(name = "VERSION")
    private Integer version;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "Profil_Permission",
        joinColumns = @JoinColumn(name = "ID_PROFIL"),
        inverseJoinColumns = @JoinColumn(name = "ID_PERMISSION")
    )
    private Set<Permission> permissions;
}
