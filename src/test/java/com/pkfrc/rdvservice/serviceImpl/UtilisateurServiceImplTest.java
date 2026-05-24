package com.pkfrc.rdvservice.serviceImpl;

import com.pkfrc.rdvservice.dto.UtilisateurRequest;
import com.pkfrc.rdvservice.dto.UtilisateurResponse;
import com.pkfrc.rdvservice.entity.Utilisateur;
import com.pkfrc.rdvservice.enumeration.Role;
import com.pkfrc.rdvservice.exception.BusinessException;
import com.pkfrc.rdvservice.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    private UtilisateurRequest utilisateurRequest;
    private Utilisateur utilisateur;
    private Utilisateur utilisateurUpdated;

    @BeforeEach
    void setUp() {
        utilisateurRequest = new UtilisateurRequest(
                "john.doe@example.com",
                "johndoe",
                "password123",
                "0612345678",
                LocalDate.of(1990, 1, 1),
                Role.CLIENT
        );

        utilisateur = Utilisateur.builder()
                .id(1L)
                .email("john.doe@example.com")
                .username("johndoe")
                .password("encodedPassword")
                .telephone("0612345678")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .role(Role.CLIENT)
                .isDeleted(false)
                .build();

        utilisateurUpdated = Utilisateur.builder()
                .id(1L)
                .email("john.updated@example.com")
                .username("johnupdated")
                .password("encodedPassword")
                .telephone("0698765432")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .role(Role.RESPONSABLE)
                .isDeleted(false)
                .build();
    }

    @Test
    void creerUtilisateur_ShouldCreateUser_WhenValidRequest() {
        when(utilisateurRepository.existsByUsernameAndIsDeletedFalse("johndoe")).thenReturn(false);
        when(utilisateurRepository.existsByEmailAndIsDeletedFalse("john.doe@example.com")).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        UtilisateurResponse response = utilisateurService.creerUtilisateur(utilisateurRequest);
        System.out.println("Utilisateur creer avec le nom: " + utilisateur.getUsername());
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("john.doe@example.com");
        assertThat(response.username()).isEqualTo("johndoe");
        assertThat(response.role()).isEqualTo(Role.CLIENT);

        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void creerUtilisateur_ShouldThrowException_WhenUsernameAlreadyExists() {
        when(utilisateurRepository.existsByUsernameAndIsDeletedFalse("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.creerUtilisateur(utilisateurRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");

        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void creerUtilisateur_ShouldThrowException_WhenEmailAlreadyExists() {
        when(utilisateurRepository.existsByUsernameAndIsDeletedFalse("johndoe")).thenReturn(false);
        when(utilisateurRepository.existsByEmailAndIsDeletedFalse("john.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.creerUtilisateur(utilisateurRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");

        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void getUtilisateurById_ShouldReturnUser_WhenUserExists() {
        when(utilisateurRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(utilisateur));

        UtilisateurResponse response = utilisateurService.getUtilisateurById(1L);
        System.out.println("Dans le test l'utilisateur est de id: "+ response.id());

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("john.doe@example.com");
    }

    @Test
    void getUtilisateurById_ShouldThrowException_WhenUserNotFound() {
//        when(utilisateurRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.getUtilisateurById(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("non trouvé");
    }

    @Test
    void getAllUtilisateurs_ShouldReturnAllActiveUsers() {
        List<Utilisateur> users = List.of(utilisateur, utilisateurUpdated);
        when(utilisateurRepository.findAllIsDeletedFalse()).thenReturn(users);

        List<UtilisateurResponse> responses = utilisateurService.getAllUtilisateurs();

        assertThat(responses).hasSize(2);
        verify(utilisateurRepository, times(1)).findAllIsDeletedFalse();
    }

    @Test
    void updateUtilisateur_ShouldUpdateUser_WhenValidRequest() {
        UtilisateurRequest updateRequest = new UtilisateurRequest(
                "john.updated@example.com",
                "johnupdated",
                "",
                "0698765432",
                LocalDate.of(1990, 1, 1),
                Role.RESPONSABLE
        );

        when(utilisateurRepository.findActiveById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.existsByUsernameAndIsDeletedFalse("johnupdated")).thenReturn(false);
        when(utilisateurRepository.existsByEmailAndIsDeletedFalse("john.updated@example.com")).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurUpdated);

        UtilisateurResponse response = utilisateurService.updateUtilisateur(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("john.updated@example.com");
        assertThat(response.username()).isEqualTo("johnupdated");
        assertThat(response.role()).isEqualTo(Role.RESPONSABLE);
    }

    @Test
    void supprimerUtilisateur_ShouldSoftDeleteUser_WhenUserExists() {
        when(utilisateurRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        utilisateurService.supprimerUtilisateur(1L);

        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
        assertThat(utilisateur.getIsDeleted()).isTrue();
    }

    @Test
    void getUtilisateursByRole_ShouldReturnUsersByRole() {
        List<Utilisateur> clients = List.of(utilisateur);
        when(utilisateurRepository.findByRoleAndIsDeletedFalse(Role.CLIENT)).thenReturn(clients);

        List<UtilisateurResponse> responses = utilisateurService.getUtilisateursByRole(Role.CLIENT);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().role()).isEqualTo(Role.CLIENT);
    }

    @Test
    void getUtilisateurByEmail_ShouldReturnUser_WhenEmailExists() {
        when(utilisateurRepository.findByEmailAndIsDeletedFalse("john.doe@example.com")).thenReturn(Optional.of(utilisateur));

        UtilisateurResponse response = utilisateurService.getUtilisateurByEmail("john.doe@example.com");

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("john.doe@example.com");
    }

    @Test
    void getUtilisateurByEmail_ShouldThrowException_WhenEmailNotFound() {
        when(utilisateurRepository.findByEmailAndIsDeletedFalse("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.getUtilisateurByEmail("nonexistent@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Utilisateur non trouvé avec email : 'nonexistent@example.com'");
    }

    @Test
    void getUtilisateurByUsername_ShouldReturnUser_WhenUsernameExists() {
        when(utilisateurRepository.findByUsernameAndIsDeletedFalse("johndoe")).thenReturn(Optional.of(utilisateur));

        UtilisateurResponse response = utilisateurService.getUtilisateurByUsername("johndoe");

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("johndoe");
    }
}