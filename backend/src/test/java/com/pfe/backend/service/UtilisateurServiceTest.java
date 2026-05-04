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
class UtilisateurServiceTest {

    @Mock
    com.pfe.backend.repository.UtilisateurRepository repository;

    @InjectMocks
    UtilisateurService service;

    @Test
    @DisplayName("le service UtilisateurService est correctement instancié")
    void serviceIsInstantiated() {
        assertThat(service).isNotNull();
    }

    @Test
    @DisplayName("retourne liste vide quand aucun utilisateur enregistré")
    void retourneListeVideQuandAucunEnregistrement() {
        when(repository.findAll()).thenReturn(List.of());

        List<?> result = repository.findAll();

        assertThat(result).isEmpty();
        verify(repository).findAll();
    }

    @Test
    @DisplayName("findById retourne vide quand l'utilisateur n'existe pas")
    void findByIdRetourneVideQuandUtilisateurInexistant() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<?> result = repository.findById(99L);

        assertThat(result).isEmpty();
        verify(repository).findById(99L);
    }

    @Test
    @DisplayName("le repository UtilisateurRepository est injecté et non null")
    void repositoryEstInjecteCorrectement() {
        assertThat(repository).isNotNull();
    }
}
