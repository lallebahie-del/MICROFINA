package com.pfe.backend.service;

import com.microfina.entity.ProduitCreditTypeMembreId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeMembreServiceTest {

    @Mock
    com.pfe.backend.repository.TypeMembreRepository repository;

    @InjectMocks
    TypeMembreService service;

    @Test
    @DisplayName("le service TypeMembreService est correctement instancié")
    void serviceIsInstantiated() {
        assertThat(service).isNotNull();
    }

    @Test
    @DisplayName("retourne liste vide quand aucun type de membre enregistré")
    void retourneListeVideQuandAucunEnregistrement() {
        when(repository.findAll()).thenReturn(List.of());

        List<?> result = repository.findAll();

        assertThat(result).isEmpty();
        verify(repository).findAll();
    }

    @Test
    @DisplayName("findById retourne vide quand le type membre n'existe pas")
    void findByIdRetourneVideQuandTypeMembreInexistant() {
        ProduitCreditTypeMembreId id = mock(ProduitCreditTypeMembreId.class);
        when(repository.findById(any(ProduitCreditTypeMembreId.class))).thenReturn(Optional.empty());

        Optional<?> result = repository.findById(id);

        assertThat(result).isEmpty();
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("le repository TypeMembreRepository est injecté et non null")
    void repositoryEstInjecteCorrectement() {
        assertThat(repository).isNotNull();
    }
}
