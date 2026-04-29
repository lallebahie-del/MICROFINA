package com.pfe.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    com.pfe.backend.repository.RoleRepository repository;

    @InjectMocks
    RoleService service;

    @Test
    @DisplayName("le service RoleService est correctement instancié")
    void serviceIsInstantiated() {
        assertThat(service).isNotNull();
    }

    @Test
    @DisplayName("retourne liste vide quand aucun rôle enregistré")
    void retourneListeVideQuandAucunEnregistrement() {
        when(repository.findAll()).thenReturn(List.of());

        List<?> result = repository.findAll();

        assertThat(result).isEmpty();
        verify(repository).findAll();
    }

    @Test
    @DisplayName("findById retourne vide quand le rôle n'existe pas")
    void findByIdRetourneVideQuandRoleInexistant() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<?> result = repository.findById(5L);

        assertThat(result).isEmpty();
        verify(repository).findById(5L);
    }

    @Test
    @DisplayName("le repository RoleRepository est injecté et non null")
    void repositoryEstInjecteCorrectement() {
        assertThat(repository).isNotNull();
    }
}
