package com.pkfrc.rdvservice.serviceImpl;


import com.pkfrc.rdvservice.dto.RendezVousRequest;
import com.pkfrc.rdvservice.dto.RendezVousResponse;
import com.pkfrc.rdvservice.entity.RendezVous;
import com.pkfrc.rdvservice.entity.Services;
import com.pkfrc.rdvservice.entity.Utilisateur;
import com.pkfrc.rdvservice.enumeration.NomService;
import com.pkfrc.rdvservice.enumeration.Role;
import com.pkfrc.rdvservice.enumeration.StatutRDV;
import com.pkfrc.rdvservice.exception.BusinessException;
import com.pkfrc.rdvservice.repository.RendezVousRepository;
import com.pkfrc.rdvservice.repository.ServiceRepository;
import com.pkfrc.rdvservice.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RdvServiceImplTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private RdvServiceImpl rdvService;

    private RendezVousRequest rendezVousRequest;
    private Utilisateur client;
    private Utilisateur responsable;
    private Services service;
    private RendezVous rendezVous;

    @BeforeEach
    void setUp() {
        client = Utilisateur.builder()
                .id(1L)
                .email("client@example.com")
                .username("client1")
                .password("45ijeusjd")
                .role(Role.CLIENT)
                .isDeleted(false)
                .build();

        responsable = Utilisateur.builder()
                .id(2L)
                .email("responsable@example.com")
                .username("responsable1")
                .password("45ijeusjd")
                .role(Role.RESPONSABLE)
                .isDeleted(false)
                .build();

        service = Services.builder()
                .id(1L)
                .nom(NomService.RH)
//                .dureeMinutes(60)
//                .estActif(true)
                .isDeleted(false)
                .build();

        rendezVousRequest = new RendezVousRequest(
                1L,  // refClient
                LocalDateTime.now().plusDays(3), // dateRDV
                "Consultation médicale", // motifRdv
                1L,  // refService
                2L,   // refResponsable
                StatutRDV.PLANIFIE
        );

        rendezVous = RendezVous.builder()
                .id(1L)
                .dateRdv(rendezVousRequest.dateRDV())
                .motifRdv(rendezVousRequest.motifRdv())
                .service(service)
                .responsable(responsable)
                .client(client)
                .isDeleted(false)
                .version(0L)
                .build();
    }

    @Test
    void creerRendezVous_ShouldCreateAppointment_WhenValidRequest() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(client));
        when(serviceRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(service));
        when(utilisateurRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(responsable));
        when(rendezVousRepository.findActiveAppointmentsByClient(1L)).thenReturn(new ArrayList<>());
        when(rendezVousRepository.findByResponsableId(2L)).thenReturn(new ArrayList<>());
        when(rendezVousRepository.findByServiceIdAndDateRdvWithLockAndClientIdAndResponsableId(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(rendezVousRepository.isTimeSlotTaken(any(), any(), any(), any())).thenReturn(false);
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(rendezVous);

        RendezVousResponse response = rdvService.creerRendezVous(rendezVousRequest);

        assertThat(response).isNotNull();
        assertThat(response.refRDV()).isEqualTo(1L);
        assertThat(response.refClient()).isEqualTo(1L);

        verify(rendezVousRepository, times(1)).save(any(RendezVous.class));
    }

    @Test
    void creerRendezVous_ShouldThrowException_WhenDateIsTooClose() {
        rendezVousRequest = new RendezVousRequest(
                1L,
                LocalDateTime.now().plusDays(1),
                "Consultation",
                1L,
                2L,
                StatutRDV.PLANIFIE
        );

        assertThatThrownBy(() -> rdvService.creerRendezVous(rendezVousRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Le rendez-vous doit être pris au moins 2 jours avant sa date. Date demandée:")
                .hasMessageContaining("Il reste seulement 0 jour(s) avant le rendez-vous.");
    }

    @Test
    void creerRendezVous_ShouldThrowException_WhenClientNotFound() {
        final Long idClient = 1L;
        when(utilisateurRepository.findById(idClient)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rdvService.creerRendezVous(rendezVousRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Client non trouvé avec l'ID: " + idClient);
    }

    @Test
    void creerRendezVous_ShouldThrowException_WhenServiceNotFound() {
        final Long id = 1L;
        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(client));
        when(serviceRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rdvService.creerRendezVous(rendezVousRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Service non trouvé avec l'ID: " + id);
    }

    @Test
    void creerRendezVous_ShouldThrowException_WhenResponsableNotFound() {
        final Long idClient = 1L;
        final Long idService = 1L;
        final Long idResponsableNotFound = 2L;
        when(utilisateurRepository.findById(idClient)).thenReturn(Optional.of(client));
        when(serviceRepository.findByIdAndIsDeletedFalse(idService)).thenReturn(Optional.of(service));
        when(utilisateurRepository.findByIdAndIsDeletedFalse(idResponsableNotFound)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rdvService.creerRendezVous(rendezVousRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Responsable non trouvé avec l'ID: " + idResponsableNotFound);
    }

    @Test
    void creerRendezVous_ShouldThrowException_WhenClientHasConflict() {
        List<RendezVous> conflictingAppointments = List.of(rendezVous);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(client));
        when(serviceRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(service));
        when(rendezVousRepository.findActiveAppointmentsByClient(1L)).thenReturn(conflictingAppointments);

        assertThatThrownBy(() -> rdvService.creerRendezVous(rendezVousRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Vous avez déjà un rendez-vous le")
                .hasMessageContaining("Impossible de créer un nouveau rendez-vous sur cette plage horaire.");
    }

    @Test
    void getRendezVousById_ShouldReturnAppointment_WhenExists() {
        when(rendezVousRepository.findActiveById(1L)).thenReturn(Optional.of(rendezVous));

        RendezVousResponse response = rdvService.getRendezVousById(1L);

        assertThat(response).isNotNull();
        assertThat(response.refRDV()).isEqualTo(1L);
    }

    @Test
    void getRendezVousById_ShouldThrowException_WhenNotFound() {
        final Long id = 999L;
        when(rendezVousRepository.findActiveById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rdvService.getRendezVousById(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Rendez-vous non trouvé avec l'ID: " + id);
    }

    @Test
    void annulerRendezVous_ShouldCancelAppointment_WhenExists() {
        when(rendezVousRepository.findActiveById(1L)).thenReturn(Optional.of(rendezVous));
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(rendezVous);

        rdvService.annulerRendezVous(1L, "Test annulation");

        verify(rendezVousRepository, times(1)).save(any(RendezVous.class));
        assertThat(rendezVous.getIsDeleted()).isTrue();
    }

    @Test
    void annulerRendezVous_ShouldThrowException_WhenAppointmentNotFound() {
        final Long idClientNotFound = 999L;
        when(rendezVousRepository.findActiveById(idClientNotFound)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rdvService.annulerRendezVous(idClientNotFound, "Test"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Rendez-vous non trouvé avec l'ID: " + idClientNotFound);
    }

    @Test
    void getRendezVousByClient_ShouldReturnClientAppointments() {
        List<RendezVous> appointments = List.of(rendezVous);
        when(utilisateurRepository.findActiveById(1L)).thenReturn(Optional.of(client));
        when(rendezVousRepository.findByClientId(1L)).thenReturn(appointments);

        List<RendezVousResponse> responses = rdvService.getRendezVousByClient(1L);

        assertThat(responses).hasSize(1);
        verify(rendezVousRepository, times(1)).findByClientId(1L);
    }

    @Test
    void getRendezVousByResponsable_ShouldReturnResponsableAppointments() {
        List<RendezVous> appointments = List.of(rendezVous);
        when(utilisateurRepository.findActiveById(2L)).thenReturn(Optional.of(responsable));
        when(rendezVousRepository.findByResponsableId(2L)).thenReturn(appointments);

        List<RendezVousResponse> responses = rdvService.getRendezVousByResponsable(2L);

        assertThat(responses).hasSize(1);
        verify(rendezVousRepository, times(1)).findByResponsableId(2L);
    }
}