package com.pfe.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String libelle;

    @Version
    private Integer version;
}
